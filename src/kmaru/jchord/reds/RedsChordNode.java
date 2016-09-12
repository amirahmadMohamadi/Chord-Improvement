package kmaru.jchord.reds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
	private Map<ChordNode, ReputationTree>				coefficients;

	public int											sharedKnuckleListSize	= Hash.KEY_LENGTH;

	public RedsChordNode(String nodeId, RedsChord redsChord)
	{
		super(nodeId, redsChord);
		this.sharedKnucklesMap = new MultiValueMap<>();
		this.coefficients = new HashMap<>();

	}

	public void initializeScoreMap()
	{
		this.scoreMap = new HashMap<>();

		Map<Integer, ReputationTree> map = null;
		for (int i = 0; i < getFingerList().size(); i++)
		{
			map = scoreMap.get(getFingerNode(i).getNodeKey());
			if (map == null)
			{
				map = new HashMap<>();

				for (int j = 0; j < getChord().bucketSize; j++)
					map.put(j, new ReputationTree(getFingerNode(i).getNodeKey(), getChord().getReputationTreeDepth(),
							getChord().getMinimumObservations()));

				scoreMap.put(getFingerNode(i).getNodeKey(), map);
			}
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
		DefaultKeyValue<ChordNode, Double> bestHelpingPeer;
		for (int i = 0; i < getChord().getHaloRedundancy(); i++)
		{
			bestHelpingPeer = calculateBestHelpingPeer(key.createEndKey(Hash.KEY_LENGTH - 1 - i),
					getChord().getScoringAlgorithm(), helpingPeers);
			helpingPeers.add(bestHelpingPeer.getKey());
		}

		List<ChordNode> results = HALocate(this, key, getChord().getHaloRedundancy(), helpingPeers);

		for (int i = 0; i < results.size() - 1; i++)
		{
			ChordNode chordNode = results.get(i);
			if (scoreMap.get(getFingerNode(i).getNodeKey()) != null)
			{
				boolean result = validateResult(chordNode, key);
				if (getChord().getScoringAlgorithm() == SharedReputationAlgorithm.Consensus)
				{
					if (i < helpingPeers.size())
						updateCoefficients(result, helpingPeers.get(i), key);
				}
				scoreMap.get(getFingerNode(i).getNodeKey()).get(0).addScore(key, result ? 1 : -1);
				incrementMessageCount(1);
			}
		}

		return reviewResults(key, results);

	}

	@Override
	protected ChordNode chordLocate(ChordKey key)
	{
		if (this == successorList.get(0))
		{
			return this;
		}

		if (key.compareTo(this.getNodeKey()) == 0)
			return this;

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
				incrementMessageCount(1);
				incrementHopCount(1);
				return ((RedsChordNode) successorList.get(0)).chordLocate(key);
			}
			if (successorList.contains(node))
			{
				incrementMessageCount(1);
				incrementHopCount(1);
				return ((RedsChordNode) node).chordLocate(key);
			}

			if (this.hasFinger(node) == false)
				throw new IllegalArgumentException();

			// Collaborative Reputation
			DefaultKeyValue<ChordNode, Integer> bestHelpingPeer = calculateBestHelpingPeerWithoutSharedReputation(key,
					node);
			incrementMessageCount(1);
			ChordNode chordLocate = ((RedsChordNode) bestHelpingPeer.getKey()).chordLocate(key);

			if (scoreMap.get(node) != null)
			{
				scoreMap.get(node).get(bestHelpingPeer.getValue()).addScore(key,
						validateResult(chordLocate, key) ? 1 : -1);
				incrementMessageCount(1);
			}
			return chordLocate;
		}
	}

	public DefaultKeyValue<ChordNode, Double> calculateBestHelpingPeer(ChordKey key,
			SharedReputationAlgorithm algorithm, List<ChordNode> omittedNodes)
	{
		List<DefaultKeyValue<ChordNode, Double>> fingersScoreMap = new ArrayList<>();

		for (int i = 0; i < getFingerList().size(); i++)
		{
			ChordNode fingerNode = getFingerNode(i);
			if (omittedNodes.contains(fingerNode))
				continue;
			switch (algorithm)
			{
			case Consensus:
				fingersScoreMap
						.add(new DefaultKeyValue<>(fingerNode, calculateHelpingPeerConsensusScore(key, fingerNode)));
				break;
			case DropOff:
				fingersScoreMap
						.add(new DefaultKeyValue<>(fingerNode, calculateHelpingPeerDropOffScore(key, fingerNode)));
				break;
			case Off:
				fingersScoreMap.add(new DefaultKeyValue<>(fingerNode,
						calculateHelpingPeerWithoutSharedReputationScore(key, fingerNode)));
			}

		}

		if (fingersScoreMap.isEmpty())
		{
			for (ChordNode chordNode : omittedNodes)
			{
				switch (algorithm)
				{
				case Consensus:
					fingersScoreMap
							.add(new DefaultKeyValue<>(chordNode, calculateHelpingPeerConsensusScore(key, chordNode)));
					break;
				case DropOff:
					fingersScoreMap
							.add(new DefaultKeyValue<>(chordNode, calculateHelpingPeerDropOffScore(key, chordNode)));
					break;
				case Off:
					fingersScoreMap.add(new DefaultKeyValue<>(chordNode,
							calculateHelpingPeerWithoutSharedReputationScore(key, chordNode)));
				}

			}
		}

		Collections.sort(fingersScoreMap, new Comparator<DefaultKeyValue<ChordNode, Double>>()
		{

			@Override
			public int compare(DefaultKeyValue<ChordNode, Double> o1, DefaultKeyValue<ChordNode, Double> o2)
			{
				return o1.getValue().compareTo(o2.getValue());
			}
		});

		return fingersScoreMap.get(0);
	}

	private DefaultKeyValue<ChordNode, Integer> calculateBestHelpingPeerWithoutSharedReputation(ChordKey key,
			ChordNode peer)
	{

		int bestHelpingPeer = 0;
		double score = -2;
		if (scoreMap.get(peer.getNodeKey()) != null)
		{
			for (Entry<Integer, ReputationTree> entry : scoreMap.get(peer.getNodeKey()).entrySet())
			{
				double storedScore = entry.getValue().getScore(key);
				if (storedScore > score)
				{
					score = storedScore;
					bestHelpingPeer = entry.getKey();
				}
			}

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

	private Double calculateHelpingPeerWithoutSharedReputationScore(ChordKey key, ChordNode peer)
	{

		if (scoreMap.get(peer.getNodeKey()) != null)
		{
			return scoreMap.get(peer.getNodeKey()).get(0).getScore(key);

		}

		return 0.0;

	}

	@SuppressWarnings("unchecked")
	public double calculateHelpingPeerDropOffScore(ChordKey key, ChordNode peer)
	{
		List<Double> scoringBin = new ArrayList<>();
		if (scoreMap.get(peer.getNodeKey()) == null)
			return 0;

		Random random = new Random(System.currentTimeMillis());
		if (sharedKnucklesMap.get(peer.getNodeKey()) != null)
			for (RedsChordNode node : (Collection<RedsChordNode>) sharedKnucklesMap.get(peer.getNodeKey()))
			{
				double peerScore = scoreMap.get(peer.getNodeKey()).get(0).getScore(key);
				incrementMessageCount(1);
				Map<Integer, ReputationTree> map = node.scoreMap.get(peer.getNodeKey());
				if (map != null)
				{
					double sharedKnucklePeerScore = map.get(0).getScore(key);
					double w = 1 - Math.abs(peerScore - sharedKnucklePeerScore) / 2;
					if (random.nextDouble() <= w)
						scoringBin.add(sharedKnucklePeerScore);
				}
			}

		scoringBin.add(scoreMap.get(peer.getNodeKey()).get(0).getScore(key));

		List<Integer> sizes;
		if (getChord().getSimulationData().getCustomProperties().get("Dynamic Size") == null)
			sizes = new ArrayList<>();
		else
			sizes = (List<Integer>) getChord().getSimulationData().getCustomProperties().get("Dynamic Size");

		sizes.add(scoringBin.size() * 4 + 8 * 2);
		getChord().getSimulationData().getCustomProperties().put("Dynamic Size", sizes);

		DescriptiveStatistics stat = new DescriptiveStatistics();
		for (Double score : scoringBin)
			stat.addValue(score);
		double bestHelpingScore = stat.getPercentile(50);

		return bestHelpingScore;
	}

	@SuppressWarnings("unchecked")
	public double calculateHelpingPeerConsensusScore(ChordKey key, ChordNode peer)
	{
		Map<RedsChordNode, List<Double>> consensusMap;
		if (getChord().getSimulationData().getCustomProperties().get("Consensus Data") == null)
			consensusMap = new HashMap<>();
		else
			consensusMap = (Map<RedsChordNode, List<Double>>) getChord().getSimulationData().getCustomProperties()
					.get("Consensus Data");

		if (scoreMap.get(peer.getNodeKey()) == null || sharedKnucklesMap.get(peer.getNodeKey()) == null)
			return 0;

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
				incrementMessageCount(2);
				if (firstVertex.hasFinger(secondVertex) || secondVertex.hasFinger(firstVertex))
					graph.addEdge(firstVertex, secondVertex);
			}
		}

		double epsilon = getChord().getSimulationData().getAlpha();
		double endLimit = 0.0001;

		consensusMap.put(this, new ArrayList<Double>());

		double diff = Double.POSITIVE_INFINITY;
		double score = scoreMap.get(peer.getNodeKey()).get(0).getScore(key);
		double newScore = score;
		while (diff > endLimit)
		{
			Map<Integer, ReputationTree> map;
			for (ConsensusGraphEdge edge : graph.edgesOf(this))
			{
				incrementMessageCount(1);
				RedsChordNode key2 = edge.getSource().equals(this) ? (RedsChordNode) edge.getTarget()
						: (RedsChordNode) edge.getSource();
				map = key2.scoreMap.get(peer.getNodeKey());
				if (map != null)
				{
					if (coefficients.get(key2) == null)
						coefficients.put(key2, new ReputationTree(getNodeKey(), getChord().getReputationTreeDepth(),
								getChord().getMinimumObservations()));
					newScore = newScore
							+ epsilon * (map.get(0).getScore(key) - score) * (coefficients.get(key2).getScore(key) + 1);
				}
			}
			diff = Math.abs(newScore - score);
			score = newScore;

			consensusMap.get(this).add(score);
		}

		List<Integer> sizes;
		if (getChord().getSimulationData().getCustomProperties().get("Dynamic Size") == null)
			sizes = new ArrayList<>();
		else
			sizes = (List<Integer>) getChord().getSimulationData().getCustomProperties().get("Dynamic Size");

		sizes.add(32 + graph.vertexSet().size() * 24 + graph.edgeSet().size() * 8);
		getChord().getSimulationData().getCustomProperties().put("Dynamic Size", sizes);

		getChord().getSimulationData().getCustomProperties().put("Consensus Data", consensusMap);
		return score;
	}

	@SuppressWarnings("unchecked")
	private void updateCoefficients(boolean result, ChordNode peer, ChordKey key)
	{
		if (scoreMap.get(peer.getNodeKey()) == null || sharedKnucklesMap.get(peer.getNodeKey()) == null)
			return;

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
				incrementMessageCount(2);
				if (firstVertex.hasFinger(secondVertex) || secondVertex.hasFinger(firstVertex))
					graph.addEdge(firstVertex, secondVertex);
			}
		}

		Map<Integer, ReputationTree> map;
		for (ConsensusGraphEdge edge : graph.edgesOf(this))
		{
			incrementMessageCount(1);
			RedsChordNode key2 = edge.getSource().equals(this) ? (RedsChordNode) edge.getTarget()
					: (RedsChordNode) edge.getSource();
			map = key2.scoreMap.get(peer.getNodeKey());
			if (map != null)
			{
				double nodeScore = map.get(0).getScore(key);
				int resultCode = (result && nodeScore > 0) || (!result && nodeScore < 0) ? 1 : -1;
				if (coefficients.get(key2) == null)
					coefficients.put(key2, new ReputationTree(getNodeKey(), getChord().getReputationTreeDepth(),
							getChord().getMinimumObservations()));
				coefficients.get(key2).addScore(key, resultCode);
			}
		}

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

	@Override
	public void fixFingers()
	{
		super.fixFingers();

		initializeScoreMap();
	}

	@Override
	public void dispose()
	{
		super.dispose();

		this.sharedKnucklesMap.clear();
		this.scoreMap.clear();
	}

	public int getStaticSize()
	{
		int scoreMapSize = 0;

		for (Entry<ChordKey, Map<Integer, ReputationTree>> entry : scoreMap.entrySet())
		{
			for (Entry<Integer, ReputationTree> entry2 : entry.getValue().entrySet())
			{
				scoreMapSize += 4 + entry2.getValue().getSize();
			}
			scoreMapSize += 20;
		}

		int sharedKnuckleSize = sharedKnucklesMap.size() * 28;

		int coefficientSize = 0;
		for (Entry<ChordNode, ReputationTree> entry : coefficients.entrySet())
		{
			coefficientSize += 24 + entry.getValue().getSize();
		}

		return scoreMapSize + sharedKnuckleSize + coefficientSize;
	}

}
