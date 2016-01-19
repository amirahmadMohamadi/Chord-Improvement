package kmaru.jchord.simulation;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import javax.swing.SwingUtilities;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import kmaru.jchord.Chord;
import kmaru.jchord.ChordException;
import kmaru.jchord.ChordNode;
import kmaru.jchord.Hash;
import kmaru.jchord.charts.NetworkDiagram;
import kmaru.jchord.charts.Plot;
import kmaru.jchord.halo.HaloChord;
import kmaru.jchord.reds.RedsChord;
import kmaru.jchord.reds.RedsChordNode;
import kmaru.jchord.reds.ScoringAlgorithm;
import kmaru.jchord.simulation.SimulationData.DEFAULT_SIMULATION_SETTINGS;

public class Simulation
{

	public static final SimulationData DEFAULT_SIMULATION_DATA = new SimulationData(true, true, true,
			EnumSet.of(ChordProtocol.REDS, ChordProtocol.HALO), DEFAULT_SIMULATION_SETTINGS.HASH_FUNCTION,
			DEFAULT_SIMULATION_SETTINGS.KEY_LENGTH, DEFAULT_SIMULATION_SETTINGS.NUM_OF_NODES,
			DEFAULT_SIMULATION_SETTINGS.NUM_OF_LOOKUPS, DEFAULT_SIMULATION_SETTINGS.MAX_MALICIOUS_PROBABILITY,
			DEFAULT_SIMULATION_SETTINGS.NUM_OF_REPEATING_TESTS, DEFAULT_SIMULATION_SETTINGS.NUM_OF_HALO_REDUNDANCY,
			DEFAULT_SIMULATION_SETTINGS.NUM_OF_BUCKET_SIZE, DEFAULT_SIMULATION_SETTINGS.REDS_MINIMUM_OBSERVATIONS,
			DEFAULT_SIMULATION_SETTINGS.REDS_REPUTATION_TREE_DEPTH, ScoringAlgorithm.DropOff);

	public Simulation()
	{
		this.simulationData = DEFAULT_SIMULATION_DATA;
	}

	public Simulation(SimulationData simulationData)
	{
		this.simulationData = simulationData;
	}

	private volatile int	progress	= 0;
	private SimulationData	simulationData;

	public void simulate() throws Exception
	{
		Map<MultiMap<Double, Double>, String> map = Collections
				.synchronizedMap(new HashMap<MultiMap<Double, Double>, String>());

		if (simulationData.getRunningSimulations().contains(ChordProtocol.Chord))
			map.put(runTest(ChordProtocol.Chord), "chord");
		if (simulationData.getRunningSimulations().contains(ChordProtocol.HALO))
			map.put(runTest(ChordProtocol.HALO), "halo " + simulationData.getHaloRedundancy());
		if (simulationData.getRunningSimulations().contains(ChordProtocol.REDS))
		{
			map.put(runTest(ChordProtocol.REDS), "reds");
		}
		if (simulationData.isResultDrawn())
			displayResult(map);
		if (simulationData.isResultSaved())
			saveResult(map);
	}

	private MultiMap<Double, Double> runTest(final ChordProtocol protocol) throws Exception
	{
		final MultiMap<Double, Double> resultMap = new MultiValueMap<>();

		// PrintStream stream = new PrintStream("result.txt");
		final PrintStream stream = System.out;

		stream.println("Testing network for protocol " + protocol);
		final Semaphore semaphore = new Semaphore(
				simulationData.getMaxFailureRate() - simulationData.getMinFailureRate() + 1);
		semaphore.acquire(simulationData.getMaxFailureRate() - simulationData.getMinFailureRate() + 1);
		ExecutorService executorService = Executors
				.newFixedThreadPool(simulationData.getMaxFailureRate() - simulationData.getMinFailureRate() + 1);

		for (int i = simulationData.getMinFailureRate(); i <= simulationData.getMaxFailureRate(); i++)
		{
			final int k = i;
			executorService.execute(new Runnable()
			{

				@Override
				public void run()
				{
					final double probability = 0.01 * k;
					for (int j = 0; j < simulationData.getRepeatingTestsNumber(); j++)
					{
						Chord chord = null;
						stream.println("maliciuos node probability is " + probability);
						try
						{
							chord = setupNetwork(probability, protocol);
							final Chord chord2 = chord;
							if (simulationData.isNetworkRingDrawn()
									&& (probability == (simulationData.getMaxFailureRate()) * 0.01 && j == 0))
							{
								try
								{
									SwingUtilities.invokeAndWait(new Runnable()
									{

										@Override
										public void run()
										{
											NetworkDiagram.drawNetwork(chord2, protocol.toString()
													+ ": malicious node probaibility = " + probability);
										}
									});
								}
								catch (InvocationTargetException | InterruptedException e)
								{
									e.printStackTrace();
								}
							}
						}
						catch (FileNotFoundException | UnknownHostException | MalformedURLException e1)
						{
							e1.printStackTrace();
						}
						try
						{
							double failureRatio = testNetwork(stream, chord, probability);
							resultMap.put(probability, failureRatio);
							stream.printf("%f\t%f\n", probability, failureRatio);

							stream.println("test ended successfuly.");
						}
						catch (Exception e)
						{
							stream.println("test failed.");
							e.printStackTrace();
						}
						progress++;
					}
					stream.println("semaphore released");
					semaphore.release();
				}
			});
		}

		while (semaphore
				.availablePermits() < (simulationData.getMaxFailureRate() - simulationData.getMinFailureRate() + 1))
			;

		return resultMap;
	}

	private double testNetwork(PrintStream stream, Chord chord, double probability)
	{
		int failedLookups = 0;
		int successfulLookups = 0;

		Random rand = new Random(System.currentTimeMillis());
		List<ChordNode> goodNodeList = chord.getGoodNodeList();
		int goodSize = goodNodeList.size();
		for (int i = 0; i < simulationData.getNumberOfLookups(); i++)
		{
			getSimulationData().getCustomProperties().put(SimulationData.KEYS.LOOKUP_NUMBER, i);

			int source = rand.nextInt(goodSize);
			int dest = rand.nextInt(goodSize);

			ChordNode sourceNode = goodNodeList.get(source);
			ChordNode destNode = goodNodeList.get(dest);

			ChordNode foundSuccessor = sourceNode.locate(destNode.getNodeKey());
			if (ChordNode.validateResult(foundSuccessor, destNode.getNodeKey()))
				successfulLookups++;
			else
				failedLookups++;

			applyChurn(chord, probability, rand);
		}

		double failureRatio = (double) failedLookups / (successfulLookups + failedLookups);
		// if (chord instanceof RedsChord)
		// {
		// System.out.println("Helps = " + ((RedsChord) chord).helps);
		// System.out.println("Helps2 = " + ((RedsChord) chord).helps2);
		// }

		return failureRatio;
	}

	private void applyChurn(Chord chord, double probability, Random rand)
	{
		List<ChordNode> goodNodeList = chord.getGoodNodeList();
		double churnProbability = 0.25 / (250 * (goodNodeList.size() / chord.getNumberOfNodes()));

		boolean update = false;
		if (rand.nextDouble() < churnProbability)
		{
			int leavingNode = rand.nextInt(chord.getNumberOfNodes());
			chord.getNode(leavingNode).leave();
			chord.deleteNode(leavingNode);

			update = true;
		}
		if (rand.nextDouble() < churnProbability)
		{
			try
			{
				ChordNode createNode;
				URL url = new URL("http", "10.0." + rand.nextInt(255) + "." + rand.nextInt(255), 9000, "");
				if (rand.nextDouble() < probability)
					createNode = chord.createMaliciousNode(url.toString());
				else
					createNode = chord.createNode(url.toString());

				createNode.join(chord.getNode(0));
			}
			catch (MalformedURLException | ChordException e)
			{
				e.printStackTrace();
			}

			update = true;
		}
		if (update)
		{
			stabalizeNetwork(null, chord);
		}
	}

	public Chord setupNetwork(double maliciousNodeProbability, ChordProtocol protocol)
			throws FileNotFoundException, UnknownHostException, MalformedURLException
	{
		PrintStream out = System.out;

		out = new PrintStream("result.log");

		// long start = System.currentTimeMillis();

		Random rand = new Random(System.currentTimeMillis());

		Hash.setFunction(simulationData.getHashFunction());
		Hash.setKeyLength(simulationData.getKeyLength());

		int numberOfMaliciousNodes = (int) (simulationData.getNumberOfNodes() * maliciousNodeProbability);

		List<Integer> maliciousNodesPlaces = new ArrayList<>();
		Random random = new Random(System.currentTimeMillis());

		while (maliciousNodesPlaces.size() < numberOfMaliciousNodes)
		{
			int nextInt = random.nextInt(simulationData.getNumberOfNodes());
			if (maliciousNodesPlaces.contains(nextInt) == false && nextInt > simulationData.getHaloRedundancy())
				maliciousNodesPlaces.add(nextInt);
		}

		Chord chord = createChord(protocol);
		int j = 0;
		while (chord.getSortedNodeMap().size() < simulationData.getNumberOfNodes())
		{
			URL url = new URL("http", "10.0." + rand.nextInt(255) + "." + rand.nextInt(255), 9000, "");
			try
			{
				if (maliciousNodesPlaces.contains(j))
					chord.createMaliciousNode(url.toString());
				else
					chord.createNode(url.toString());
				j++;
			}
			catch (ChordException e)
			{
				// e.printStackTrace();
			}
		}
		out.println(simulationData.getNumberOfNodes() + " nodes are created.");

		for (int i = 0; i < simulationData.getNumberOfNodes(); i++)
		{
			ChordNode node = chord.getSortedNode(i);
			out.println(node);
		}

		for (int i = 1; i < chord.getNumberOfNodes(); i++)
		{
			ChordNode node = chord.getNode(i);
			node.join(chord.getNode(0));
			ChordNode preceding = node.getSuccessor().getPredecessor();
			node.stabilize();
			if (preceding == null)
			{
				node.getSuccessor().stabilize();
			}
			else
			{
				preceding.stabilize();
			}
		}

		stabalizeNetwork(out, chord);

		for (int i = 0; i < simulationData.getNumberOfNodes(); i++)
		{
			ChordNode node = chord.getSortedNode(i);
			node.printFingerTable(out);
		}

		// long end = System.currentTimeMillis();
		//
		// int interval = (int) (end - start);
		// System.out.printf("%d nodes are malicious.\n",
		// chord.getMaliciousNodeList().size());
		// System.out.printf("Elapsed Time : %d.%d\n", interval / 1000, interval
		// % 1000);

		out.close();
		return chord;
	}

	private void stabalizeNetwork(PrintStream out, Chord chord)
	{

		for (int i = 0; i < chord.getNumberOfNodes(); i++)
		{
			ChordNode node = chord.getNode(i);

			if (node.getPredecessor() == null)
				node.stabilize();
		}

		for (int j1 = 0; j1 < 10; j1++)
			for (int i = 0; i < chord.getNumberOfNodes(); i++)
			{
				ChordNode node = chord.getNode(i);
				node.stabilize();
			}
		if (out != null)
			out.println("Chord ring is established.");

		for (int j1 = 0; j1 < 10; j1++)
			for (int i = 0; i < chord.getNumberOfNodes(); i++)
			{
				ChordNode node = chord.getNode(i);
				node.fixSuccessorList();
			}
		for (int j1 = 0; j1 < 10; j1++)
			for (int i = 0; i < chord.getNumberOfNodes(); i++)
			{
				ChordNode node = chord.getNode(i);
				node.stabilize();
			}

//		for (int i = 0; i < chord.getNumberOfNodes(); i++)
//		{
//			ChordNode node = chord.getNode(i);
//			node.validateSuccessorList();
//			if (node.getPredecessor() == null)
//				throw new IllegalStateException("Predecessor is null.");
//		}
//		if (out != null)
//			out.println("Successor lists are fixed.");

		for (int i = 0; i < chord.getNumberOfNodes(); i++)
		{
			ChordNode node = chord.getNode(i);
			node.fixFingers();
		}
		if (out != null)
			out.println("Finger Tables are fixed.");

		if (chord.getProtocol() == ChordProtocol.REDS)
		{
			for (int i = 0; i < chord.getNumberOfNodes(); i++)
			{
				RedsChordNode node = (RedsChordNode) chord.getNode(i);
				node.initializeScoreMap();
				node.fixSharedKnuckles();
			}
			if (out != null)
				out.println("Shared Knuckle maps are fixed.");
		}

	}

	private Chord createChord(ChordProtocol protocol)
	{
		Chord chord = null;
		switch (protocol)
		{
		case Chord:
			chord = new Chord(0);
			break;
		case HALO:
			chord = new HaloChord(0, simulationData.getHaloRedundancy());
			break;
		case REDS:
			chord = new RedsChord(0, simulationData.getHaloRedundancy(), simulationData.getBucketSize(),
					simulationData.getRedsReputationTreeDepth(), simulationData.getRedsMinObservations(),
					simulationData.getScoringAlgorithm());
			break;
		default:
			return null;
		}
		chord.setSimulationData(getSimulationData());
		return chord;
	}

	@SuppressWarnings("unchecked")
	private static void displayResult(Map<MultiMap<Double, Double>, String> resultMaps)
	{
		Plot plot = new Plot();
		for (MultiMap<Double, Double> resultMap : resultMaps.keySet())
		{
			List<Double> meanList = new ArrayList<>();
			List<Double> stdList = new ArrayList<>();

			SummaryStatistics stat;

			List<Double> x = new ArrayList<>();
			x.addAll(resultMap.keySet());
			Collections.sort(x);

			for (Double maliciousNodeProbability : x)
			{
				stat = new SummaryStatistics();
				Collection<Double> collection = (Collection<Double>) resultMap.get(maliciousNodeProbability);
				for (Double value : collection)
				{
					stat.addValue(value);
				}

				double mean = stat.getMean();
				double standardDeviation = stat.getStandardDeviation();

				meanList.add(mean);
				stdList.add(standardDeviation);
			}

			plot.addDataset(x, meanList, stdList, resultMaps.get(resultMap));
		}
		plot.draw();
	}

	@SuppressWarnings("unchecked")
	private static void saveResult(Map<MultiMap<Double, Double>, String> resultMaps) throws FileNotFoundException
	{
		try (PrintStream stream = new PrintStream("result.csv"))
		{
			for (MultiMap<Double, Double> resultMap : resultMaps.keySet())
			{
				List<Double> meanList = new ArrayList<>();
				List<Double> stdList = new ArrayList<>();

				SummaryStatistics stat;

				List<Double> x = new ArrayList<>();
				x.addAll(resultMap.keySet());
				Collections.sort(x);

				stream.println("\n" + resultMaps.get(resultMap));
				try (PrintStream matlabFile = new PrintStream(resultMaps.get(resultMap) + ".csv"))
				{
					for (Double maliciousNodeProbability : x)
					{
						stat = new SummaryStatistics();
						Collection<Double> collection = (Collection<Double>) resultMap.get(maliciousNodeProbability);
						for (Double value : collection)
						{
							stat.addValue(value);
							matlabFile.print(value + ", ");
						}

						double mean = stat.getMean();
						double standardDeviation = stat.getStandardDeviation();

						meanList.add(mean);
						stdList.add(standardDeviation);
					}
				}
				stream.println();
				stream.print("Mean,");
				for (Double mean : meanList)
				{
					stream.print(" " + mean + ",");
				}
				stream.println();
				stream.print("Standard Deviation,");
				for (Double std : stdList)
				{
					stream.print(" " + std + ",");
				}
				stream.println();
			}
		}

	}

	public int getProgress()
	{
		return progress;
	}

	public void setProgress(int progress)
	{
		this.progress = progress;
	}

	public SimulationData getSimulationData()
	{
		return simulationData;
	}

	public void setSimulationData(SimulationData simulationData)
	{
		this.simulationData = simulationData;
	}

}
