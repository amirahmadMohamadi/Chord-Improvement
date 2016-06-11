package kmaru.jchord.halo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import kmaru.jchord.Chord;
import kmaru.jchord.ChordException;
import kmaru.jchord.ChordNode;
import kmaru.jchord.simulation.ChordProtocol;

public class HaloChord extends Chord
{

	int	haloRedundancy;

	public HaloChord(double maliciousNodeProbability, int haloRedundancy)
	{
		super(maliciousNodeProbability);
		this.haloRedundancy = haloRedundancy;
	}

	@Override
	public ChordNode createNode(String nodeId) throws ChordException
	{
		Random rand = new Random(System.nanoTime());
		ChordNode node;
		if (rand.nextInt(100) < maliciousNodeProbability * 100)
			node = new MaliciousHaloNode(nodeId, this);
		else
			node = new HaloChordNode(nodeId, this);

		nodeList.add(node);

		if (sortedNodeMap.get(node.getNodeKey()) != null)
		{
			throw new ChordException("Duplicated Key: " + node);
		}

		sortedNodeMap.put(node.getNodeKey(), node);
		
		return node;
	}

	@Override
	public ChordNode createMaliciousNode(String nodeId) throws ChordException
	{
		ChordNode node = new MaliciousHaloNode(nodeId, this);

		nodeList.add(node);

		if (sortedNodeMap.get(node.getNodeKey()) != null)
		{
			throw new ChordException("Duplicated Key: " + node);
		}

		sortedNodeMap.put(node.getNodeKey(), node);
		
		return node;
	}

	public int getHaloRedundancy()
	{
		return this.haloRedundancy;
	}

	public List<ChordNode> getTrustedNodes()
	{
		List<ChordNode> trustedNodes = new ArrayList<>();

		for (int i = 0; i < haloRedundancy; i++)
		{
			trustedNodes.add(getNode(i));
		}
		return trustedNodes;
	}

	@Override
	public List<ChordNode> getMaliciousNodeList()
	{
		List<ChordNode> list = new ArrayList<>();

		for (ChordNode node : nodeList)
		{
			if (node instanceof MaliciousHaloNode)
				list.add(node);
		}

		return list;
	}

	@Override
	public ChordProtocol getProtocol()
	{
		return ChordProtocol.HALO;
	}

}
