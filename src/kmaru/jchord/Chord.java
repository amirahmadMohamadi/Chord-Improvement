package kmaru.jchord;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import kmaru.jchord.simulation.ChordProtocol;
import kmaru.jchord.simulation.SimulationData;

public class Chord
{

	private SimulationData						simulationData;
	protected double							maliciousNodeProbability	= 0;
	protected List<ChordNode>					nodeList;
	protected SortedMap<ChordKey, ChordNode>	sortedNodeMap;
	Object[]									sortedKeyArray;

	public Chord(double maliciousNodeProbability)
	{
		this.maliciousNodeProbability = maliciousNodeProbability;
		this.nodeList = new ArrayList<ChordNode>();
		this.sortedNodeMap = new TreeMap<ChordKey, ChordNode>();

	}

	public ChordNode createNode(String nodeId) throws ChordException
	{
		Random rand = new Random(System.nanoTime());
		ChordNode node;
		if (rand.nextInt(100) < maliciousNodeProbability * 100)
			node = new MaliciousChordNode(nodeId, this);
		else
			node = new ChordNode(nodeId, this);

		nodeList.add(node);

		if (sortedNodeMap.get(node.getNodeKey()) != null)
		{
			throw new ChordException("Duplicated Key: " + node);
		}

		sortedNodeMap.put(node.getNodeKey(), node);

		return node;
	}

	public ChordNode createMaliciousNode(String nodeId) throws ChordException
	{
		ChordNode node = new MaliciousChordNode(nodeId, this);

		nodeList.add(node);

		if (sortedNodeMap.get(node.getNodeKey()) != null)
		{
			throw new ChordException("Duplicated Key: " + node);
		}

		sortedNodeMap.put(node.getNodeKey(), node);

		return node;
	}

	public void deleteNode(int i)
	{
		ChordNode node = getNode(i);
		nodeList.remove(node);
		sortedNodeMap.remove(node.getNodeKey());
		sortedKeyArray = sortedNodeMap.keySet().toArray();
	}

	public ChordNode getNode(int i)
	{
		return (ChordNode) nodeList.get(i);
	}

	public ChordNode getSortedNode(int i)
	{
		if (sortedKeyArray == null)
		{
			sortedKeyArray = sortedNodeMap.keySet().toArray();
		}
		return (ChordNode) sortedNodeMap.get(sortedKeyArray[i]);
	}

	public double getMaliciousNodeProbability()
	{
		return this.maliciousNodeProbability;
	}

	public List<ChordNode> getMaliciousNodeList()
	{
		List<ChordNode> list = new ArrayList<>();

		for (ChordNode node : nodeList)
		{
			if (node instanceof MaliciousChordNode)
				list.add(node);
		}

		return list;
	}

	public List<ChordNode> getGoodNodeList()
	{
		List<ChordNode> list = new ArrayList<>(nodeList);

		list.removeAll(getMaliciousNodeList());

		return list;
	}

	public SortedMap<ChordKey, ChordNode> getSortedNodeMap()
	{
		return sortedNodeMap;
	}

	public void setSortedNodeMap(SortedMap<ChordKey, ChordNode> sortedNodeMap)
	{
		this.sortedNodeMap = sortedNodeMap;
	}

	public SimulationData getSimulationData()
	{
		return simulationData;
	}

	public void setSimulationData(SimulationData simulationData)
	{
		this.simulationData = simulationData;
		getSimulationData().getCustomProperties().put("Message count", 0);
		getSimulationData().getCustomProperties().put("Hop count", new ArrayList<Integer>());
	}

	public ChordProtocol getProtocol()
	{
		return ChordProtocol.Chord;
	}

	public int getNumberOfNodes()
	{
		return nodeList.size();
	}
}
