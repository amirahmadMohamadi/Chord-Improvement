package kmaru.jchord.reds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import kmaru.jchord.ChordKey;
import kmaru.jchord.ChordNode;
import kmaru.jchord.Hash;
import kmaru.jchord.halo.HaloChordNode;

public class RedsChordNode extends HaloChordNode
{
	Map<Integer, Map<Integer, ReputationTree>>	scoreMap;
	MultiMap<Integer, RedsChordNode>			sharedKnucklesMap;

	public int sharedKnuckleListSize = Hash.KEY_LENGTH;

	public RedsChordNode(String nodeId, RedsChord redsChord)
	{
		super(nodeId, redsChord);
		this.scoreMap = new HashMap<>();
		this.sharedKnucklesMap = new MultiValueMap<>();

		Map<Integer, ReputationTree> map;
		for (int i = 0; i < redsChord.getHaloRedundancy(); i++)
			for (int j = 0; j < redsChord.bucketSize; j++)
			{
				map = scoreMap.get(i);
				if (map == null)
					map = new HashMap<>();
				map.put(j, new ReputationTree(getNodeKey().createStartKey(i)));

				scoreMap.put(i, map);
			}

	}

	@Override
	public RedsChord getChord()
	{
		return (RedsChord) super.getChord();
	}

	@Override
	public ChordNode locate(ChordKey key)
	{
		List<ChordNode> helpingPeers = new ArrayList<>();
		List<Integer> helpingPeerNumbers = new ArrayList<>();
		ChordNode node;
		for (int i = 0; i < getChord().getHaloRedundancy(); i++)
		{
			int bestHelpingPeer = calculateBestHelpingPeer(key, i);

			node = getFingerTable().getFinger(i).getNode();
			for (int k = 0; k < bestHelpingPeer; k++)
			{
				if (node.getPredecessor() == null)
				{
					bestHelpingPeer = k;
					break;
				}
				node = node.getPredecessor();
			}
			helpingPeers.add(node);
			helpingPeerNumbers.add(bestHelpingPeer);
		}

		List<ChordNode> results = HALocate(this, key, getChord().getHaloRedundancy(), helpingPeers);

		for (int i = 0; i < results.size() - 1; i++)
		{
			ChordNode chordNode = results.get(i);
			scoreMap.get(i).get(helpingPeerNumbers.get(i)).addScore(key, chordNode.getNodeKey().equals(key) ? 1 : -1);
		}

		return reviewResults(key, results);

	}

	@SuppressWarnings("unchecked")
	private int calculateBestHelpingPeer(ChordKey key, int i)
	{
		MultiMap<Integer, Double> scoringBin = new MultiValueMap<>();

		Random random = new Random(System.currentTimeMillis());
		if (sharedKnucklesMap.get(i) != null)
			for (RedsChordNode node : (Collection<RedsChordNode>) sharedKnucklesMap.get(i))
			{
				for (int j = 0; j < getChord().bucketSize; j++)
				{
					double peerScore = scoreMap.get(i).get(j).getScore(key.createEndKey(Hash.KEY_LENGTH - 1 - i));
					double sharedKnucklePeerScore = node.scoreMap.get(i).get(j)
							.getScore(key.createEndKey(Hash.KEY_LENGTH - 1 - i));

					double w = 1 - Math.abs(peerScore - sharedKnucklePeerScore);
					if (random.nextDouble() <= w)
						scoringBin.put(j, sharedKnucklePeerScore);
				}
			}

		for (int j = 0; j < getChord().bucketSize; j++)
		{
			scoringBin.put(j, scoreMap.get(i).get(j).getScore(key.createEndKey(Hash.KEY_LENGTH - 1 - i)));
		}

		double bestHelpingScore = 0;
		int bestHelpingPeer = 0;
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

		return bestHelpingPeer;

	}

	public void fixSharedKnuckles()
	{
		ChordNode finger;
		ChordNode knuckleSearch;
		for (int i = 0; i < getChord().getHaloRedundancy(); i++)
		{
			finger = getFingerTable().getFinger(i).getNode();
			for (int j = 0; j < sharedKnuckleListSize; j++)
			{
				knuckleSearch = knuckleSearch(finger.getNodeKey(), j);
				if (knuckleSearch.getFingerTable().getFinger(Hash.KEY_LENGTH - 1 - j).getNode().equals(finger))
					sharedKnucklesMap.put(i, knuckleSearch);
			}
		}
	}
}
