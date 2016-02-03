package kmaru.jchord.reds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.Random;

import kmaru.jchord.ChordException;
import kmaru.jchord.ChordKey;
import kmaru.jchord.ChordNode;
import kmaru.jchord.halo.HaloChord;
import kmaru.jchord.simulation.ChordProtocol;

public class RedsChord extends HaloChord
{

	int									bucketSize;
	private int							reputationTreeDepth;
	private int							minimumObservations;
	private SharedReputationAlgorithm	scoringAlgorithm;

	public RedsChord(double maliciousNodeProbability, int haloRedundancy, int bucketSize, int reputationTreDepth,
			int minimumObservations, SharedReputationAlgorithm scoringAlgorithm)
	{
		super(maliciousNodeProbability, haloRedundancy);
		this.bucketSize = bucketSize;
		this.reputationTreeDepth = reputationTreDepth;
		this.minimumObservations = minimumObservations;
		this.scoringAlgorithm = scoringAlgorithm;
	}

	@Override
	public ChordNode createNode(String nodeId) throws ChordException
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

		return node;
	}

	@Override
	public ChordNode createMaliciousNode(String nodeId) throws ChordException
	{
		ChordNode node = new MaliciousRedsNode(nodeId, this);

		nodeList.add(node);

		if (sortedNodeMap.get(node.getNodeKey()) != null)
		{
			throw new ChordException("Duplicated Key: " + node);
		}

		sortedNodeMap.put(node.getNodeKey(), node);

		return node;
	}

	public int getReputationTreeDepth()
	{
		return reputationTreeDepth;
	}

	public int getMinimumObservations()
	{
		return minimumObservations;
	}

	public SharedReputationAlgorithm getScoringAlgorithm()
	{
		return scoringAlgorithm;
	}

	@Override
	public List<ChordNode> getMaliciousNodeList()
	{
		List<ChordNode> list = new ArrayList<>();

		for (ChordNode node : nodeList)
		{
			if (node instanceof MaliciousRedsNode)
				list.add(node);
		}

		return list;
	}

	@Override
	public ChordProtocol getProtocol()
	{
		return ChordProtocol.REDS;
	}

	/**
	 * This method gathers all scores of all nodes from nodes' reputation trees.
	 * @return
	 */
	public Map<ChordKey, DescriptiveStatistics> summarizeScores()
	{
		Map<ChordKey, DescriptiveStatistics> map = new HashMap<>();

		for (Entry<ChordKey, ChordNode> entry : getSortedNodeMap().entrySet())
		{
			for (ChordNode node : entry.getValue().getFingerList())
			{
				ChordNode itNode = node;
				for (Entry<Integer, ReputationTree> entry2 : ((RedsChordNode) entry.getValue()).getScoreMap()
						.get(node.getNodeKey()).entrySet())
				{
					ChordKey key = itNode.getNodeKey();
					if (map.containsKey(key) == false)
						map.put(key, new DescriptiveStatistics());

					map.get(key).addValue(entry2.getValue().getRoot().getScore());
					
					if (itNode.getPredecessor() == null)
						break;
					itNode = itNode.getPredecessor();
				}
				
			}
			
		}

		return map;
	}

}
