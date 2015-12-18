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

public class Simulation
{

	public static class SimulationData
	{
		private boolean					isNetworkRingDrawn;
		private boolean					isResultDrawn;
		private boolean					isResultSaved;
		private EnumSet<ChordProtocol>	runningSimulations;
		private String					hashFunction;
		private int						keyLength;
		private int						numberOfNodes;
		private int						numberOfLookups;
		private int						maxFailureRate;
		private int						minFailureRate;
		private int						repeatingTestsNumber;
		private int						haloRedundancy;
		private int						bucketSize;
		private int						redsMinObservations;
		private int						redsReputationTreeDepth;
		private ScoringAlgorithm		scoringAlgorithm;

		public SimulationData()
		{
			this.hashFunction = DEFAULT_SIMULATION_SETTINGS.HASH_FUNCTION;
			this.keyLength = DEFAULT_SIMULATION_SETTINGS.KEY_LENGTH;
			this.runningSimulations = EnumSet.noneOf(ChordProtocol.class);
			scoringAlgorithm = ScoringAlgorithm.DropOff;
			this.minFailureRate = 0;
		}

		public SimulationData(SimulationData simulationData)
		{
			this(simulationData.isNetworkRingDrawn, simulationData.isResultDrawn, simulationData.isResultSaved,
					simulationData.runningSimulations, simulationData.hashFunction, simulationData.keyLength,
					simulationData.numberOfNodes, simulationData.numberOfLookups, simulationData.maxFailureRate,
					simulationData.repeatingTestsNumber, simulationData.haloRedundancy, simulationData.bucketSize,
					simulationData.redsMinObservations, simulationData.redsReputationTreeDepth,
					simulationData.scoringAlgorithm);
		}

		public SimulationData(boolean isNetworkRingDrawn, boolean isResultDrawn, boolean isResultSaved,
				EnumSet<ChordProtocol> runningSimulations, String hashFunction, int keyLength, int numberOfNodes,
				int numberOfLookups, int maxFailureRate, int repeatingTestsNumber, int haloRedundancy, int bucketSize,
				int redsMinObservations, int redsReputationTreeDepth, ScoringAlgorithm scoringAlgorithm)
		{
			this.isNetworkRingDrawn = isNetworkRingDrawn;
			this.isResultDrawn = isResultDrawn;
			this.isResultSaved = isResultSaved;
			this.runningSimulations = runningSimulations;
			this.hashFunction = hashFunction;
			this.keyLength = keyLength;
			this.numberOfNodes = numberOfNodes;
			this.numberOfLookups = numberOfLookups;
			this.maxFailureRate = maxFailureRate;
			this.repeatingTestsNumber = repeatingTestsNumber;
			this.haloRedundancy = haloRedundancy;
			this.bucketSize = bucketSize;
			this.redsMinObservations = redsMinObservations;
			this.redsReputationTreeDepth = redsReputationTreeDepth;
			this.scoringAlgorithm = scoringAlgorithm;

			this.minFailureRate = 0;
		}

		public boolean isNetworkRingDrawn()
		{
			return isNetworkRingDrawn;
		}

		public void setNetworkRingDrawn(boolean isNetworkRingDrawn)
		{
			this.isNetworkRingDrawn = isNetworkRingDrawn;
		}

		public boolean isResultDrawn()
		{
			return isResultDrawn;
		}

		public void setResultDrawn(boolean isResultDrawn)
		{
			this.isResultDrawn = isResultDrawn;
		}

		public boolean isResultSaved()
		{
			return isResultSaved;
		}

		public void setResultSaved(boolean isResultSaved)
		{
			this.isResultSaved = isResultSaved;
		}

		public EnumSet<ChordProtocol> getRunningSimulations()
		{
			return runningSimulations;
		}

		public void setRunningSimulations(EnumSet<ChordProtocol> runningSimulations)
		{
			this.runningSimulations = runningSimulations;
		}

		public String getHashFunction()
		{
			return hashFunction;
		}

		public void setHashFunction(String hashFunction)
		{
			this.hashFunction = hashFunction;
		}

		public int getKeyLength()
		{
			return keyLength;
		}

		public void setKeyLength(int keyLength)
		{
			this.keyLength = keyLength;
		}

		public int getNumberOfNodes()
		{
			return numberOfNodes;
		}

		public void setNumberOfNodes(int numberOfNodes)
		{
			this.numberOfNodes = numberOfNodes;
		}

		public int getNumberOfLookups()
		{
			return numberOfLookups;
		}

		public void setNumberOfLookups(int numberOfLookups)
		{
			this.numberOfLookups = numberOfLookups;
		}

		public int getMaxFailureRate()
		{
			return maxFailureRate;
		}

		public void setMaxFailureRate(int maxFailureRate)
		{
			this.maxFailureRate = maxFailureRate;
		}

		public int getMinFailureRate()
		{
			return minFailureRate;
		}

		public void setMinFailureRate(int minFailureRate)
		{
			this.minFailureRate = minFailureRate;
		}

		public int getRepeatingTestsNumber()
		{
			return repeatingTestsNumber;
		}

		public void setRepeatingTestsNumber(int repeatingTestsNumber)
		{
			this.repeatingTestsNumber = repeatingTestsNumber;
		}

		public int getHaloRedundancy()
		{
			return haloRedundancy;
		}

		public void setHaloRedundancy(int haloRedundancy)
		{
			this.haloRedundancy = haloRedundancy;
		}

		public int getBucketSize()
		{
			return bucketSize;
		}

		public void setBucketSize(int bucketSize)
		{
			this.bucketSize = bucketSize;
		}

		public int getRedsMinObservations()
		{
			return redsMinObservations;
		}

		public void setRedsMinObservations(int redsMinObservations)
		{
			this.redsMinObservations = redsMinObservations;
		}

		public int getRedsReputationTreeDepth()
		{
			return redsReputationTreeDepth;
		}

		public void setRedsReputationTreeDepth(int redsReputationTreeDepth)
		{
			this.redsReputationTreeDepth = redsReputationTreeDepth;
		}

		public ScoringAlgorithm getScoringAlgorithm()
		{
			return this.scoringAlgorithm;
		}

		public void setScoringAlgorithm(ScoringAlgorithm scoringAlgorithm)
		{
			this.scoringAlgorithm = scoringAlgorithm;
		}
	}

	public static class DEFAULT_SIMULATION_SETTINGS
	{
		public static final String	HASH_FUNCTION				= "SHA-1";
		public static final int		KEY_LENGTH					= 160;
		public static final int		NUM_OF_NODES				= 100;
		public static final int		NUM_OF_LOOKUPS				= 1000;
		public static final int		MAX_MALICIOUS_PROBABILITY	= 31;
		public static final int		NUM_OF_REPEATING_TESTS		= 10;
		public static final int		NUM_OF_HALO_REDUNDANCY		= 5;
		public static final int		NUM_OF_BUCKET_SIZE			= 2;
		public static final int		REDS_MINIMUM_OBSERVATIONS	= 3;
		public static final int		REDS_REPUTATION_TREE_DEPTH	= 130;
	}

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
		final Semaphore semaphore = new Semaphore(simulationData.getMaxFailureRate());
		semaphore.acquire(simulationData.getMaxFailureRate());
		ExecutorService executorService = Executors.newFixedThreadPool(simulationData.getMaxFailureRate());

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
							double failureRatio = testNetwork(stream, chord);
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

		while (semaphore.availablePermits() < (simulationData.getMaxFailureRate()- simulationData.getMinFailureRate() + 1))
			;

		return resultMap;
	}

	private double testNetwork(PrintStream stream, Chord chord)
	{
		int failedLookups = 0;
		int successfulLookups = 0;

		if (chord instanceof RedsChord)
		{
			((RedsChord) chord).helps = 0;
			((RedsChord) chord).helps2 = 0;
		}

		Random rand = new Random(System.currentTimeMillis());
		int goodSize = chord.getGoodNodeList().size();
		for (int i = 0; i < simulationData.getNumberOfLookups(); i++)
		{
			int source = rand.nextInt(goodSize);
			int dest = rand.nextInt(goodSize);

			ChordNode sourceNode = chord.getGoodNodeList().get(source);
			ChordNode destNode = chord.getGoodNodeList().get(dest);

			ChordNode foundSuccessor = sourceNode.locate(destNode.getNodeKey());
			if (foundSuccessor.getNodeId().equals(destNode.getNodeId()))
				successfulLookups++;
			else
				failedLookups++;
		}

		double failureRatio = (double) failedLookups / (successfulLookups + failedLookups);
//		if (chord instanceof RedsChord)
//		{
//			System.out.println("Helps = " + ((RedsChord) chord).helps);
//			System.out.println("Helps2 = " + ((RedsChord) chord).helps2);
//		}
		return failureRatio;
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
//				e.printStackTrace();
			}
		}
		out.println(simulationData.getNumberOfNodes() + " nodes are created.");

		for (int i = 0; i < simulationData.getNumberOfNodes(); i++)
		{
			ChordNode node = chord.getSortedNode(i);
			out.println(node);
		}

		for (int i = 1; i < simulationData.getNumberOfNodes(); i++)
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

		for (int j1 = 0; j1 < 10; j1++)
			for (int i = 0; i < simulationData.getNumberOfNodes(); i++)
			{
				ChordNode node = chord.getNode(i);
				node.stabilize();
			}
		out.println("Chord ring is established.");

		out.println("Successor lists are fixed.");

		for (int i = 0; i < simulationData.getNumberOfNodes(); i++)
		{
			ChordNode node = chord.getNode(i);
			node.fixFingers();
		}
		out.println("Finger Tables are fixed.");

		for (int i = 0; i < simulationData.getNumberOfNodes(); i++)
		{
			ChordNode node = chord.getSortedNode(i);
			node.printFingerTable(out);
		}

		if (protocol == ChordProtocol.REDS)
		{
			for (int i = 0; i < simulationData.getNumberOfNodes(); i++)
			{
				RedsChordNode node = (RedsChordNode) chord.getNode(i);
				node.initializeScoreMap();
				node.fixSharedKnuckles();
			}
			out.println("Shared Knuckle maps are fixed.");
		}
		// long end = System.currentTimeMillis();
		//
		// int interval = (int) (end - start);
		// System.out.printf("%d nodes are malicious.\n",
		// chord.getMaliciousNodeList().size());
		// System.out.printf("Elapsed Time : %d.%d\n", interval / 1000, interval
		// % 1000);

		return chord;
	}

	private Chord createChord(ChordProtocol protocol)
	{
		switch (protocol)
		{
		case Chord:
			return new Chord(0);
		case HALO:
			return new HaloChord(0, simulationData.getHaloRedundancy());
		case REDS:
			return new RedsChord(0, simulationData.getHaloRedundancy(), simulationData.getBucketSize(),
					simulationData.getRedsReputationTreeDepth(), simulationData.getRedsMinObservations(),
					simulationData.getScoringAlgorithm());
		default:
			return null;
		}
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

				stream.print(resultMaps.get(resultMap));
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

	// public boolean isNetworkRingDrawn()
	// {
	// return data.isNetworkRingDrawn();
	// }
	//
	// public void setNetworkRingDrawn(boolean isNetworkRingDrawn)
	// {
	// this.data.setNetworkRingDrawn(isNetworkRingDrawn);
	// }
	//
	// public boolean isResultDrawn()
	// {
	// return data.isResultDrawn();
	// }
	//
	// public void setResultDrawn(boolean isResultDrawn)
	// {
	// this.data.setResultDrawn(isResultDrawn);
	// }
	//
	// public boolean isResultSaved()
	// {
	// return data.isResultSaved();
	// }
	//
	// public void setResultSaved(boolean isResultSaved)
	// {
	// this.data.setResultSaved(isResultSaved);
	// }
	//
	// public EnumSet<ChordProtocol> getRunningSimulations()
	// {
	// return data.getRunningSimulations();
	// }
	//
	// public void setRunningSimulations(EnumSet<ChordProtocol>
	// runningSimulations)
	// {
	// this.data.setRunningSimulations(runningSimulations);
	// }
	//
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
