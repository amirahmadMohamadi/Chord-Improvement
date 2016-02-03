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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import kmaru.jchord.Chord;
import kmaru.jchord.ChordException;
import kmaru.jchord.ChordKey;
import kmaru.jchord.ChordNode;
import kmaru.jchord.Hash;
import kmaru.jchord.charts.ConsensusPlot;
import kmaru.jchord.charts.FailureRatioPlot;
import kmaru.jchord.charts.NetworkDiagram;
import kmaru.jchord.halo.HaloChord;
import kmaru.jchord.reds.RedsChord;
import kmaru.jchord.reds.RedsChordNode;
import kmaru.jchord.reds.SharedReputationAlgorithm;

public class Simulation
{

	private Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	private volatile int	progress	= 0;
	private SimulationData	simulationData;

	public Simulation()
	{
		this(SimulationData.DEFAULT_SIMULATION_DATA);
	}

	public Simulation(SimulationData simulationData)
	{
		this.simulationData = simulationData;
		ConsoleHandler handler = null;
		for (Handler handler2 : logger.getHandlers())
		{
			if (handler2 instanceof ConsoleHandler)
				handler = (ConsoleHandler) handler2;
		}
		if (handler != null)
			logger.removeHandler(handler);
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

	public Logger getLogger()
	{
		return logger;
	}

	public void simulate() throws Exception
	{
		Map<MultiMap<Double, Double>, String> map = Collections
				.synchronizedMap(new HashMap<MultiMap<Double, Double>, String>());

		for (ChordProtocol chordProtocol : simulationData.getRunningSimulations())
			map.put(runTest(chordProtocol), getSimulationName(chordProtocol, simulationData));

		if (simulationData.isResultDrawn())
			displayResult(map);
		if (simulationData.isResultSaved())
			saveResult(map);
	}

	public String getSimulationName(ChordProtocol chordProtocol, SimulationData simulationData)
	{
		switch (chordProtocol)
		{
		case Chord:
			return "chord";
		case HALO:
			return "halo " + simulationData.getHaloRedundancy();
		case REDS:
			return "reds (collaborative"
					+ (simulationData.getSharedReputationAlgorithm() == SharedReputationAlgorithm.Off ? ""
							: ("-" + simulationData.getSharedReputationAlgorithm()))
					+ ")";
		}
		throw new IllegalStateException();
	}

	private MultiMap<Double, Double> runTest(final ChordProtocol protocol) throws Exception
	{
		final MultiMap<Double, Double> resultMap = new MultiValueMap<>();

		getLogger().log(Level.INFO, "Testing network for protocol " + protocol);
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
					Chord chord;
					for (int j = 0; j < simulationData.getRepeatingTestsNumber(); j++)
					{
						chord = null;
						getLogger().log(Level.INFO, "maliciuos node probability is " + probability);
						try
						{
							chord = setupNetwork(probability, protocol);
							final Chord chord2 = chord;
							postNetworkSetup(protocol, probability, j, chord2);

							preSimulation(probability, chord);
							double failureRatio = testNetwork(chord, probability, simulationData.getNumberOfLookups());
							postSimulation(probability, j, chord);

							resultMap.put(probability, failureRatio);
							getLogger().log(Level.INFO, String.format("%f\t%f\n", probability, failureRatio));
							getLogger().log(Level.INFO, "test ended successfuly.");

							for (ChordNode chordNode : chord.getSortedNodeMap().values())
							{
								chordNode.dispose();
							}
							chord.getSortedNodeMap().clear();
							chord = null;
						}
						catch (Exception e)
						{
							getLogger().log(Level.INFO, "test failed.");
							e.printStackTrace();
						}
						progress++;
					}
					getLogger().log(Level.INFO, "semaphore released");

					semaphore.release();
				}

			});
		}

		while (semaphore
				.availablePermits() < (simulationData.getMaxFailureRate() - simulationData.getMinFailureRate() + 1))
			;

		return resultMap;
	}

	private double testNetwork(Chord chord, double probability, int numberOfTests)
	{
		int failedLookups = 0;
		int successfulLookups = 0;

		Random rand = new Random(System.currentTimeMillis());
		List<ChordNode> goodNodeList = chord.getGoodNodeList();
		int goodSize = goodNodeList.size();
		for (int i = 0; i < numberOfTests; i++)
		{
			getSimulationData().getCustomProperties().put(SimulationData.KEYS.LOOKUP_NUMBER, i);
			getLogger().log(Level.INFO, i + " ");
			int source = rand.nextInt(goodSize);
			int dest = rand.nextInt(goodSize);

			ChordNode sourceNode = goodNodeList.get(source);
			ChordNode destNode = goodNodeList.get(dest);

			ChordNode foundSuccessor = sourceNode.locate(destNode.getNodeKey());
			if (ChordNode.validateResult(foundSuccessor, destNode.getNodeKey()))
				successfulLookups++;
			else
				failedLookups++;

			if (getSimulationData().isChurnEnabled())
				applyChurn(chord, probability, rand);
		}

		double failureRatio = (double) failedLookups / (successfulLookups + failedLookups);

		return failureRatio;
	}

	private void postNetworkSetup(final ChordProtocol protocol, final double probability, int j, final Chord chord2)
	{
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
						NetworkDiagram.drawNetwork(chord2,
								protocol.toString() + ": malicious node probaibility = " + probability);
					}
				});
			}
			catch (InvocationTargetException | InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	private void preSimulation(final double probability, Chord chord)
	{
		if (chord.getProtocol() == ChordProtocol.REDS)
			testNetwork(chord, probability, 250);
	}

	private void postSimulation(final double probability, int j, Chord chord)
	{
		if (simulationData.getRunningSimulations().contains(ChordProtocol.REDS)
				&& (probability == (simulationData.getMaxFailureRate()) * 0.01 && j == 0))
		{
			estimateMaliciosNodes((RedsChord) chord);
			
			if (simulationData.getSharedReputationAlgorithm() == SharedReputationAlgorithm.Consensus)
				saveConsensusInformation(chord);
		}
	}

	
	private void applyChurn(Chord chord, double probability, Random rand)
	{
		List<ChordNode> goodNodeList = chord.getGoodNodeList();
		double churnProbability = 0.25
				/ ((chord.getNumberOfNodes() / 4) * (goodNodeList.size() / chord.getNumberOfNodes()));

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
			boolean added = false;
			while (added == false)
				try
				{
					ChordNode createNode;
					URL url = new URL("http", "10.0." + rand.nextInt(255) + "." + rand.nextInt(255), 9000, "");
					if (rand.nextDouble() < probability)
						createNode = chord.createMaliciousNode(url.toString());
					else
						createNode = chord.createNode(url.toString());

					createNode.join(chord.getNode(0));
					added = true;
				}
				catch (MalformedURLException | ChordException e)
				{
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

		stabalizeNetwork(getLogger(), chord);

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

	private void stabalizeNetwork(Logger logger, Chord chord)
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
		if (logger != null)
			logger.log(Level.INFO, "Chord ring is established.");

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

		// for (int i = 0; i < chord.getNumberOfNodes(); i++)
		// {
		// ChordNode node = chord.getNode(i);
		// node.validateSuccessorList();
		// if (node.getPredecessor() == null)
		// throw new IllegalStateException("Predecessor is null.");
		// }
		// if (out != null)
		// out.println("Successor lists are fixed.");

		for (int i = 0; i < chord.getNumberOfNodes(); i++)
		{
			ChordNode node = chord.getNode(i);
			node.fixFingers();
		}
		if (logger != null)
			logger.log(Level.INFO, "Finger Tables are fixed.");

		if (chord.getProtocol() == ChordProtocol.REDS)
		{
			for (int i = 0; i < chord.getNumberOfNodes(); i++)
			{
				RedsChordNode node = (RedsChordNode) chord.getNode(i);
				node.initializeScoreMap();
				node.fixSharedKnuckles();
			}
			if (logger != null)
				logger.log(Level.INFO, "Shared Knuckle maps are fixed.");
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
					simulationData.getSharedReputationAlgorithm());
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
		final FailureRatioPlot plot = new FailureRatioPlot();
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

		invoke(new Runnable()
		{

			@Override
			public void run()
			{
				plot.draw();
			}
		});
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

	protected void saveConsensusInformation(Chord chord)
	{
		final ConsensusPlot plot = new ConsensusPlot();
		@SuppressWarnings("unchecked")
		Map<RedsChordNode, List<Double>> consensusMap = (Map<RedsChordNode, List<Double>>) chord.getSimulationData()
				.getCustomProperties().get("Consensus Data");

		int j = 0;
		try (PrintStream stream = new PrintStream("consensus.csv"))
		{
			for (Entry<RedsChordNode, List<Double>> entry : consensusMap.entrySet())
			{
				stream.print(entry.getKey().getNodeId() + ", ");
				double[][] scores = new double[2][entry.getValue().size()];
				for (int i = 0; i < entry.getValue().size(); i++)
				{
					scores[0][i] = i;
					scores[1][i] = entry.getValue().get(i);
					stream.print(scores[1][i] + ", ");
				}
				stream.println();
				j++;
				if (j < 10)
					plot.addDataset(scores, entry.getKey().getNodeId());
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}

		invoke(new Runnable()
		{

			@Override
			public void run()
			{
				plot.draw();
			}
		});
	}

	public void estimateMaliciosNodes(RedsChord reds)
	{
		Map<ChordKey, DescriptiveStatistics> map = reds.summarizeScores();
		
		List<ChordKey> maliciousNodes = new ArrayList<>();
		for (Entry<ChordKey, DescriptiveStatistics> entry : map.entrySet())
		{
			// We consider the average of all scores of one node as its overall score.
			if (entry.getValue().getMean() < 0)
				maliciousNodes.add(entry.getKey());
		}
		

		int undetected = 0;
		List<ChordNode> realMaliciousNodeList = reds.getMaliciousNodeList();
		List<ChordKey> realMaliciousKeys = new ArrayList<>();
		for (ChordNode node : realMaliciousNodeList)
		{
			realMaliciousKeys.add(node.getNodeKey());
		}
		
		for (ChordKey chordKey : realMaliciousKeys)
		{
			if (maliciousNodes.contains(chordKey) == false)
				undetected++;
		}
		int falseDetected = 0;
		for (ChordKey chordKey : maliciousNodes)
		{
			if (realMaliciousKeys.contains(chordKey) == false)
				falseDetected++;
		}
		int detected = realMaliciousNodeList.size() - undetected;
		
		System.out.println("Undetected = " + undetected);
		System.out.println("False Detected = " + falseDetected);
		System.out.println("Detected = " + detected);
	}
	
	public static void invoke(Runnable runnable)
	{
		if (SwingUtilities.isEventDispatchThread())
		{
			runnable.run();
			return;
		}
		try
		{
			SwingUtilities.invokeAndWait(runnable);
		}
		catch (InvocationTargetException | InterruptedException e)
		{
			e.printStackTrace();
		}
	}

}
