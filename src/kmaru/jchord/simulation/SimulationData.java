package kmaru.jchord.simulation;

import java.util.EnumSet;
import java.util.Properties;

public class SimulationData
{
	private boolean						isNetworkRingDrawn;
	private boolean						isResultDrawn;
	private boolean						isResultSaved;
	private EnumSet<ChordProtocol>		runningSimulations;
	private String						hashFunction;
	private int							keyLength;
	private int							numberOfNodes;
	private int							numberOfLookups;
	private int							maxFailureRate;
	private int							minFailureRate;
	private int							repeatingTestsNumber;
	private int							haloRedundancy;
	private int							bucketSize;
	private int							redsMinObservations;
	private int							redsReputationTreeDepth;
	private boolean						isChurnEnabled;
	private double						alpha;

	private Properties					customParameters;

	public static final SimulationData	DEFAULT_SIMULATION_DATA	= new SimulationData(true, true, true,
			EnumSet.of(ChordProtocol.REDS, ChordProtocol.HALO), DEFAULT_SIMULATION_SETTINGS.HASH_FUNCTION,
			DEFAULT_SIMULATION_SETTINGS.KEY_LENGTH, DEFAULT_SIMULATION_SETTINGS.NUM_OF_NODES,
			DEFAULT_SIMULATION_SETTINGS.NUM_OF_LOOKUPS, DEFAULT_SIMULATION_SETTINGS.MAX_MALICIOUS_PROBABILITY,
			DEFAULT_SIMULATION_SETTINGS.NUM_OF_REPEATING_TESTS, DEFAULT_SIMULATION_SETTINGS.NUM_OF_HALO_REDUNDANCY,
			DEFAULT_SIMULATION_SETTINGS.NUM_OF_BUCKET_SIZE, DEFAULT_SIMULATION_SETTINGS.REDS_MINIMUM_OBSERVATIONS,
			DEFAULT_SIMULATION_SETTINGS.REDS_REPUTATION_TREE_DEPTH);

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

	public static class KEYS
	{
		public static final String LOOKUP_NUMBER = "lookupNumber";
	}

	public SimulationData()
	{
		this.hashFunction = DEFAULT_SIMULATION_SETTINGS.HASH_FUNCTION;
		this.keyLength = DEFAULT_SIMULATION_SETTINGS.KEY_LENGTH;
		this.runningSimulations = EnumSet.noneOf(ChordProtocol.class);
		this.minFailureRate = 0;
		this.customParameters = new Properties();
		alpha = 0.1;
	}

	public SimulationData(SimulationData simulationData)
	{
		this(simulationData.isNetworkRingDrawn, simulationData.isResultDrawn, simulationData.isResultSaved,
				simulationData.runningSimulations, simulationData.hashFunction, simulationData.keyLength,
				simulationData.numberOfNodes, simulationData.numberOfLookups, simulationData.maxFailureRate,
				simulationData.repeatingTestsNumber, simulationData.haloRedundancy, simulationData.bucketSize,
				simulationData.redsMinObservations, simulationData.redsReputationTreeDepth);
	}

	public SimulationData(boolean isNetworkRingDrawn, boolean isResultDrawn, boolean isResultSaved,
			EnumSet<ChordProtocol> runningSimulations, String hashFunction, int keyLength, int numberOfNodes,
			int numberOfLookups, int maxFailureRate, int repeatingTestsNumber, int haloRedundancy, int bucketSize,
			int redsMinObservations, int redsReputationTreeDepth)
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

		this.alpha = 0.1;
		this.minFailureRate = 0;
		this.customParameters = new Properties();
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

	public Properties getCustomProperties()
	{
		return this.customParameters;
	}

	public boolean isChurnEnabled()
	{
		return isChurnEnabled;
	}

	public void setChurnEnabled(boolean churn)
	{
		this.isChurnEnabled = churn;
	}

	public double getAlpha()
	{
		return alpha;
	}

	public void setAlpha(double alpha)
	{
		this.alpha = alpha;
	}
}
