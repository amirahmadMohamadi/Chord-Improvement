package kmaru.jchord.test;

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
import kmaru.jchord.reds.ReputationTree;

public class Simulation
{

	public static class DEFAULT_SIMULATION_SETTINGS
	{
		public static final String	HASH_FUNCTION				= "SHA-1";
		public static final int		KEY_LENGTH					= 160;
		public static final int		NUM_OF_NODES				= 100;
		public static final int		NUM_OF_LOOKUPS				= 2000;
		public static final int		MAX_MALICIOUS_PROBABILITY	= 31;
		public static final int		NUM_OF_REPEATING_TESTS		= 10;
		public static final int		NUM_OF_HALO_REDUNDANCY		= 5;
		public static final int		NUM_OF_BUCKET_SIZE			= 2;
		public static final int		REDS_MINIMUM_OBSERVATIONS	= 3;
		public static final int		REDS_REPUTATION_TREE_DEPTH	= 1;
	}

	private boolean					isNetworkRingDrawn	= true;
	private boolean					isResultDrawn		= true;
	private boolean					isResultSaved		= true;
	private EnumSet<ChordProtocol>	runningSimulations	= EnumSet.of(ChordProtocol.REDS, ChordProtocol.HALO);
	private volatile int			progress			= 0;

	private String	hashFunction			= DEFAULT_SIMULATION_SETTINGS.HASH_FUNCTION;
	private int		keyLength				= DEFAULT_SIMULATION_SETTINGS.KEY_LENGTH;
	private int		numberOfNodes			= DEFAULT_SIMULATION_SETTINGS.NUM_OF_NODES;
	private int		numberOfLookups			= DEFAULT_SIMULATION_SETTINGS.NUM_OF_LOOKUPS;
	private int		maxFailureRate			= DEFAULT_SIMULATION_SETTINGS.MAX_MALICIOUS_PROBABILITY;
	private int		repeatingTestsNumber	= DEFAULT_SIMULATION_SETTINGS.NUM_OF_REPEATING_TESTS;
	private int		haloRedundancy			= DEFAULT_SIMULATION_SETTINGS.NUM_OF_HALO_REDUNDANCY;
	private int		bucketSize				= DEFAULT_SIMULATION_SETTINGS.NUM_OF_BUCKET_SIZE;
	private int		redsMinObservations		= DEFAULT_SIMULATION_SETTINGS.REDS_MINIMUM_OBSERVATIONS;
	private int		redsReputationTreeDepth	= DEFAULT_SIMULATION_SETTINGS.REDS_REPUTATION_TREE_DEPTH;

	public void simulate() throws Exception
	{
		Map<MultiMap<Double, Double>, String> map = Collections
				.synchronizedMap(new HashMap<MultiMap<Double, Double>, String>());

		if (runningSimulations.contains(ChordProtocol.Chord))
			map.put(runTest(ChordProtocol.Chord), "chord");
		if (runningSimulations.contains(ChordProtocol.HALO))
			map.put(runTest(ChordProtocol.HALO), "halo " + getHaloRedundancy());
		if (runningSimulations.contains(ChordProtocol.REDS))
		{
			ReputationTree.MINIMUM_OBSERVATIONS = getRedsMinObservations();
			ReputationTree.TREE_DEPTH = getRedsReputationTreeDepth();
			map.put(runTest(ChordProtocol.REDS), "reds");
		}
		if (isResultDrawn())
			displayResult(map);
		if (isResultSaved())
			saveResult(map);
	}

	private MultiMap<Double, Double> runTest(final ChordProtocol protocol) throws Exception
	{
		final MultiMap<Double, Double> resultMap = new MultiValueMap<>();

		// PrintStream stream = new PrintStream("result.txt");
		final PrintStream stream = System.out;

		stream.println("Testing network for protocol " + protocol);
		final Semaphore semaphore = new Semaphore(getMaxFailureRate());
		semaphore.acquire(getMaxFailureRate());
		ExecutorService executorService = Executors.newFixedThreadPool(getMaxFailureRate());

		for (int i = 0; i < getMaxFailureRate(); i++)
		{
			final int k = i;
			executorService.execute(new Runnable()
			{

				@Override
				public void run()
				{
					final double probability = 0.01 * k;
					for (int j = 0; j < getRepeatingTestsNumber(); j++)
					{
						Chord chord = null;
						stream.println("maliciuos node probability is " + probability);
						try
						{
							chord = setupNetwork(probability, protocol);
							final Chord chord2 = chord;
							if (isNetworkRingDrawn() && (probability == (getMaxFailureRate() - 1) * 0.01 && j == 0))
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

		while (semaphore.availablePermits() < getMaxFailureRate())
			;

		return resultMap;
	}

	private double testNetwork(PrintStream stream, Chord chord)
	{
		int failedLookups = 0;
		int successfulLookups = 0;

		Random rand = new Random(System.currentTimeMillis());
		int goodSize = chord.getGoodNodeList().size();
		for (int i = 0; i < getNumberOfLookups(); i++)
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

		return failureRatio;
	}

	private Chord setupNetwork(double maliciousNodeProbability, ChordProtocol protocol)
			throws FileNotFoundException, UnknownHostException, MalformedURLException
	{
		PrintStream out = System.out;

		out = new PrintStream("result.log");

		// long start = System.currentTimeMillis();

		Random rand = new Random(System.currentTimeMillis());

		Hash.setFunction(getHashFunction());
		Hash.setKeyLength(getKeyLength());

		int numberOfMaliciousNodes = (int) (getNumberOfNodes() * maliciousNodeProbability);

		List<Integer> maliciousNodesPlaces = new ArrayList<>();
		Random random = new Random(System.currentTimeMillis());

		while (maliciousNodesPlaces.size() < numberOfMaliciousNodes)
		{
			int nextInt = random.nextInt(getNumberOfNodes());
			if (maliciousNodesPlaces.contains(nextInt) == false && nextInt > getHaloRedundancy())
				maliciousNodesPlaces.add(nextInt);
		}

		Chord chord = createChord(protocol);
		int j = 0;
		while (chord.getSortedNodeMap().size() < getNumberOfNodes())
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
				e.printStackTrace();
			}
		}
		out.println(getNumberOfNodes() + " nodes are created.");

		for (int i = 0; i < getNumberOfNodes(); i++)
		{
			ChordNode node = chord.getSortedNode(i);
			out.println(node);
		}

		for (int i = 1; i < getNumberOfNodes(); i++)
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
			for (int i = 0; i < getNumberOfNodes(); i++)
			{
				ChordNode node = chord.getNode(i);
				node.stabilize();
			}
		out.println("Chord ring is established.");

		out.println("Successor lists are fixed.");

		for (int i = 0; i < getNumberOfNodes(); i++)
		{
			ChordNode node = chord.getNode(i);
			node.fixFingers();
		}
		out.println("Finger Tables are fixed.");

		for (int i = 0; i < getNumberOfNodes(); i++)
		{
			ChordNode node = chord.getSortedNode(i);
			node.printFingerTable(out);
		}

		if (protocol == ChordProtocol.REDS)
		{
			for (int i = 0; i < getNumberOfNodes(); i++)
			{
				RedsChordNode node = (RedsChordNode) chord.getNode(i);
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
			return new HaloChord(0, getHaloRedundancy());
		case REDS:
			return new RedsChord(0, getHaloRedundancy(), getBucketSize());
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

	public int getProgress()
	{
		return progress;
	}

	public void setProgress(int progress)
	{
		this.progress = progress;
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

	public void setKeyLength(int keyength)
	{
		this.keyLength = keyength;
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
}
