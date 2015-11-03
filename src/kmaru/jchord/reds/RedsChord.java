package kmaru.jchord.reds;

import java.util.Random;

import kmaru.jchord.ChordException;
import kmaru.jchord.ChordNode;
import kmaru.jchord.halo.HaloChord;

public class RedsChord extends HaloChord
{

	int bucketSize;
	public RedsChord(double maliciousNodeProbability, int haloRedundancy, int bucketSize)
	{
		super(maliciousNodeProbability, haloRedundancy);
		this.bucketSize = bucketSize;
	}

	public void createNode(String nodeId) throws ChordException
	{
		Random rand = new Random(System.nanoTime());
		ChordNode node;
		if (rand.nextInt(100) < maliciousNodeProbability * 100)
			node = new MaliciousRedsNode(nodeId, this);
		else
			node = new RedsChordNode(nodeId, this);

		nodeList.add(node);

		if (sortedNodeMap.get(node.getNodeKey()) != null)
		{
			throw new ChordException("Duplicated Key: " + node);
		}

		sortedNodeMap.put(node.getNodeKey(), node);
	}

	public void createMaliciousNode(String nodeId) throws ChordException
	{
		ChordNode node = new MaliciousRedsNode(nodeId, this);

		nodeList.add(node);

		if (sortedNodeMap.get(node.getNodeKey()) != null)
		{
			throw new ChordException("Duplicated Key: " + node);
		}

		sortedNodeMap.put(node.getNodeKey(), node);
	}

}
