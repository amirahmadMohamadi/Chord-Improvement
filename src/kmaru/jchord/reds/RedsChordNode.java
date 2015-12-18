package kmaru.jchord.reds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;
import org.apache.commons.collections4.map.MultiValueMap;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jgrapht.graph.SimpleGraph;

import kmaru.jchord.ChordKey;
import kmaru.jchord.ChordNode;
import kmaru.jchord.Hash;
import kmaru.jchord.halo.HaloChordNode;

public class RedsChordNode extends HaloChordNode
{
	private Map<ChordKey, Map<Integer, ReputationTree>>	scoreMap;
	private MultiMap<ChordKey, RedsChordNode>			sharedKnucklesMap;

	public int sharedKnuckleListSize = Hash.KEY_LENGTH;

	public RedsChordNode(String nodeId, RedsChord redsChord)
	{
		super(nodeId, redsChord);
		this.sharedKnucklesMap = new MultiValueMap<>();

		initializeScoreMap();
	}

	public void initializeScoreMap()
	{
		this.scoreMap = new HashMap<>();

		Map<Integer, ReputationTree> map = null;
		for (int i = 0; i < sharedKnuckleListSize; i++)
		{
			map = scoreMap.get(getFingerNode(i).getNodeKey());
			if (map == null)
				map = new HashMap<>();

			for (int j = 0; j < getChord().bucketSize; j++)
				map.put(j, new ReputationTree(getFingerNode(i).getNodeKey(), getChord().getReputationTreeDepth(),
						getChord().getMinimumObservations()));

			scoreMap.put(getFingerNode(i).getNodeKey(), map);
		}
	}

	@Override
	public RedsChord getChord()
	{
		return (RedsChord) super.getChord();
	}

	public Map<ChordKey, Map<Integer, ReputationTree>> getScoreMap()
	{
		return scoreMap;
	}

	public MultiMap<ChordKey, RedsChordNode> getSharedKnucklesMap()
	{
		return sharedKnucklesMap;
	}

	@Override
	public ChordNode locate(ChordKey key)
	{
		List<ChordNode> helpingPeers = new ArrayList<>();
		List<Integer> helpingPeerNumbers = new ArrayList<>();
		DefaultKeyValue<ChordNode, Integer> bestHelpingPeer;
		for (int i = 0; i < getChord().getHaloRedundancy(); i++)
		{
			bestHelpingPeer = calculateBestHelpingPeer(key.createEndKey(Hash.KEY_LENGTH - 1 - i), getFingerNode(i));
			helpingPeers.add(bestHelpingPeer.getKey());
			helpingPeerNumbers.add(bestHelpingPeer.getValue());
		}

		List<ChordNode> results = HALocate(this, key, getChord().getHaloRedundancy(), helpingPeers);

		for (int i = 0; i < results.size() - 1; i++)
		{
			ChordNode chordNode = results.get(i);
			scoreMap.get(getFingerNode(i).getNodeKey()).get(helpingPeerNumbers.get(i)).addScore(key,
					chordNode.getNodeKey().equals(key) ? 1 : -1);
		}

		return reviewResults(key, results);

	}

	protected ChordNode chordLocate(ChordKey key)
	{
		if (this == successorList.get(0))
		{
			return this;
		}

		if (key.isBetween(this.getNodeKey(), successorList.get(0).getNodeKey())
				|| key.compareTo(successorList.get(0).getNodeKey()) == 0)
		{
			return successorList.get(0);
		}
		else
		{
			ChordNode node = closestPrecedingNode(key);
			if (node == this)
			{
				return ((RedsChordNode) successorList.get(0)).chordLocate(key);
			}
			if (successorList.contains(node))
				return ((RedsChordNode) node).chordLocate(key);

			if (this.hasFinger(node) == false)
				throw new IllegalArgumentException();

			DefaultKeyValue<ChordNode,Integer> bestHelpingPeer = calculateBestHelpingPeer(key, node);
			ChordNode chordLocate = ((RedsChordNode) bestHelpingPeer.getKey()).chordLocate(key);

			if (scoreMap.get(node) != null)
				scoreMap.get(node).get(bestHelpingPeer.getValue()).addScore(key,
						chordLocate.getNodeKey().equals(key) ? 1 : -1);
			return chordLocate;
		}
	}

	public DefaultKeyValue<ChordNode, Integer> calculateBestHelpingPeer(ChordKey key, ChordNode peer)
	{
		int bestHelpingPeer = 0;
		switch (getChord().getScoringAlgorithm())
		{
		case Consensus:
			bestHelpingPeer = calculateBestHelpingPeerConsensus(key, peer);
		case DropOff:
			bestHelpingPeer = calculateBestHelpingPeerDropOff(key, peer);
		case Off:
			bestHelpingPeer = calculateBestHelpingPeerWithoutSharedReputation(key, peer);
			break;
		}

		ChordNode node = peer;
		for (int k = 0; k < bestHelpingPeer; k++)
		{
			if (node.getPredecessor() == null)
			{
				bestHelpingPeer = k;
				break;
			}
			node = node.getPredecessor();
		}

		return new DefaultKeyValue<ChordNode, Integer>(node, bestHelpingPeer);

	}

	private int calculateBestHelpingPeerWithoutSharedReputation(ChordKey key, ChordNode peer)
	{

		int bestHelpingPeer = 0;
		double score = 0;
		if (scoreMap.get(peer.getNodeKey()) != null)
		{
			for (Entry<Integer, ReputationTree> entry : scoreMap.get(peer.getNodeKey()).entrySet())
			{
				if (entry.getKey() > score)
				{
					score = entry.getKey();
					bestHelpingPeer=entry.getKey();
				}
			}

		}
		return bestHelpingPeer;
	}

	@SuppressWarnings("unchecked")
	public int calculateBestHelpingPeerDropOff(ChordKey key, ChordNode peer)
	{
		MultiMap<Integer, Double> scoringBin = new MultiValueMap<>();
		int bestHelpingPeer = 0;
		if (scoreMap.get(peer.getNodeKey()) == null)
			return bestHelpingPeer;
		
		Random random = new Random(System.currentTimeMillis());
		if (sharedKnucklesMap.get(peer.getNodeKey()) != null)
			for (RedsChordNode node : (Collection<RedsChordNode>) sharedKnucklesMap.get(peer.getNodeKey()))
			{
				for (int j = 0; j < getChord().bucketSize; j++)
				{
					double peerScore = scoreMap.get(peer.getNodeKey()).get(j).getScore(key);
					Map<Integer, ReputationTree> map = node.scoreMap.get(peer.getNodeKey());
					if (map != null)
					{
						double sharedKnucklePeerScore = map.get(j).getScore(key);
						if (sharedKnucklePeerScore != 0.5)
							getChord().helps2++;
						getChord().helps++;
						double w = 1 - Math.abs(peerScore - sharedKnucklePeerScore);
						if (random.nextDouble() <= w)
							scoringBin.put(j, sharedKnucklePeerScore);
					}
				}
			}

		for (int j = 0; j < getChord().bucketSize; j++)
		{
			scoringBin.put(j, scoreMap.get(peer.getNodeKey()).get(j).getScore(key));
		}

		double bestHelpingScore = 0;
		
		DescriptiveStatistics stat;
		for (int j = 0; j < getChord().bucketSize; j++)
		{
			stat = new DescriptiveStatistics();
			for (Double score : (Collection<Double>) scoringBin.get(j))
				stat.addValue(score);
			if (stat.getPercentile(50) > bestHelpingScore)
			{
				bestHelpingScore = stat.getPercentile(50);
				bestHelpingPeer = j;
			}
		}
		System.out.println("best score = " + bestHelpingScore + " peer = " + bestHelpingPeer);

		return bestHelpingPeer;

	}

	@SuppressWarnings("unchecked")
	public int calculateBestHelpingPeerConsensus(ChordKey key, ChordNode peer)
	{
		int bestHelpingPeer = 0;
		if (scoreMap.get(peer.getNodeKey()) == null)
			return bestHelpingPeer;

		SimpleGraph<RedsChordNode, ConsensusGraphEdge> graph = new SimpleGraph<>(ConsensusGraphEdge.class);

		for (RedsChordNode node : (Collection<RedsChordNode>) sharedKnucklesMap.get(peer.getNodeKey()))
			graph.addVertex(node);
		graph.addVertex(this);

		for (RedsChordNode firstVertex : graph.vertexSet())
		{
			for (RedsChordNode secondVertex : graph.vertexSet())
			{
				if (firstVertex.equals(secondVertex))
					continue;
				if (firstVertex.hasFinger(secondVertex) || secondVertex.hasFinger(firstVertex))
					graph.addEdge(firstVertex, secondVertex);
			}
		}

		double epsilon = 0.1;
		List<Double> scores = new ArrayList<>();
		for (int k = 0; k < getChord().bucketSize; k++)
		{
			scores.add(scoreMap.get(peer.getNodeKey()).get(k).getScore(key));
		}

		for (int j = 0; j < getChord().bucketSize; j++)
		{
			double diff = Double.POSITIVE_INFINITY;
			double score = scores.get(j);
			double newScore = score;
			while (diff > 0.1)
			{
				for (ConsensusGraphEdge edge : graph.edgesOf(this))
				{
					if (edge.getSource().equals(this))
					{
						Map<Integer, ReputationTree> map = ((RedsChordNode) edge.getTarget()).scoreMap
								.get(peer.getNodeKey());
						if (map != null)
							newScore = newScore + epsilon * (map.get(j).getScore(key) - score);
					}
					else
					{
						Map<Integer, ReputationTree> map = ((RedsChordNode) edge.getSource()).scoreMap
								.get(peer.getNodeKey());
						if (map != null)
							newScore = newScore + epsilon * (map.get(j).getScore(key) - score);
					}
				}
				diff = Math.abs(newScore - score);
				score = newScore;
			}
			scores.set(j, score);
		}

		for (int l = 0; l < scores.size(); l++)
		{
			if (scores.get(l) > scores.get(bestHelpingPeer))
				bestHelpingPeer = l;

		}
		return bestHelpingPeer;
	}

	@SuppressWarnings("unchecked")
	public void fixSharedKnuckles()
	{
		ChordNode finger;
		ChordNode knuckleSearch;
		for (int i = 0; i < getChord().getHaloRedundancy(); i++)
		{
			finger = getFingerNode(i);
			for (int j = 0; j < sharedKnuckleListSize; j++)
			{
				knuckleSearch = knuckleSearch(finger.getNodeKey(), j);
				if (knuckleSearch.getFingerTable().getFinger(Hash.KEY_LENGTH - 1 - j).getNode().equals(finger)
						&& (sharedKnucklesMap.get(i) == null || ((Collection<RedsChordNode>) sharedKnucklesMap.get(i))
								.contains(knuckleSearch) == false))
					sharedKnucklesMap.put(finger.getNodeKey(), knuckleSearch);
			}
		}
	}

}
