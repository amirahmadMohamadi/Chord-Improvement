package kmaru.jchord.halo;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import kmaru.jchord.ChordKey;
import kmaru.jchord.ChordNode;
import kmaru.jchord.Finger;
import kmaru.jchord.Hash;

public class HaloChordNode extends ChordNode
{

	public HaloChordNode(String nodeId, HaloChord haloChord)
	{
		super(nodeId, haloChord);
	}

	@Override
	public ChordNode locate(ChordKey key)
	{
		List<ChordNode> results = HALocate(this, key, getChord().haloRedundancy, null);

		return reviewResults(key, results);
	}

	public List<ChordNode> HALocate(ChordNode node, ChordKey key, int redundancy, List<ChordNode> trustedNodes)
	{
		List<ChordNode> knuckleList = new ArrayList<>();
		// node.printFingerTable(System.out);
		List<ChordNode> resultList = new ArrayList<>();
		for (int i = 0; i < redundancy - 1; i++)
		{
			ChordNode knuckle = null;
			ChordKey knuckleKey = key.createEndKey(Hash.KEY_LENGTH - 1 - i);

			if (trustedNodes == null)
			{
				ChordNode finger = node.getFingerNode(i);
				incrementMessageCount(1);
				insertNewHopCount();
				knuckle = ((HaloChordNode) finger).chordLocate(knuckleKey);
			}
			else
			{
				incrementMessageCount(1);
				insertNewHopCount();
				if (trustedNodes.size() > i)
					knuckle = ((HaloChordNode) trustedNodes.get(i)).chordLocate(knuckleKey);
				else
					knuckle = ((HaloChordNode) node).chordLocate(knuckleKey);
			}

			if (knuckle == null)
				throw new IllegalStateException("locate result is null");

			ChordNode improvedKnuckle = improveKnucleEstimate(knuckle, key, knuckleKey, i);
			knuckleList.add(improvedKnuckle);

			incrementMessageCount(1);
			ChordNode finger = improvedKnuckle.getFingerTable().getFinger(Hash.KEY_LENGTH - 1 - i).getNode();

			// TODO this makes results better but I don't know why this happens
			// if (validateResult(finger.getPredecessor(), key))
			// finger = finger.getPredecessor();
			resultList.add(finger);
		}
		incrementMessageCount(1);
		ChordNode chordResult = ((HaloChordNode) node).chordLocate(key);
		resultList.add(chordResult);

		return resultList;
	}

	/**
	 * Returns a node that it's ith finger is the specified key. The algorithm
	 * is based on knuckle search algorithm
	 *
	 * @param key
	 * @param i
	 * @return
	 */
	protected ChordNode knuckleSearch(ChordKey key, int i)
	{
		ChordKey knuckleKey = key.createEndKey(Hash.KEY_LENGTH - 1 - i);
		incrementMessageCount(1);
		ChordNode knuckle = chordLocate(knuckleKey);
		return improveKnucleEstimate(knuckle, key, knuckleKey, i);
	}

	protected ChordNode chordLocate(ChordKey key)
	{
		incrementMessageCount(1);
		if (this == successorList.get(0))
		{
			return this;
		}

		incrementMessageCount(1);
		if (validateResult(this, key))
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
				return ((HaloChordNode) successorList.get(0)).chordLocate(key);
			}

			incrementMessageCount(1);
			incrementHopCount(1);
			return ((HaloChordNode) node).chordLocate(key);
		}
	}

	protected ChordNode reviewResults(ChordKey key, List<ChordNode> resultList)
	{

		SortedMap<ChordKey, ChordNode> results = new TreeMap<>();

		for (ChordNode chordNode : resultList)
		{
			results.put(chordNode.getNodeKey(), chordNode);
		}

		for (ChordKey chordKey : results.keySet())
		{
			if (chordKey.compareTo(key) >= 0)
				return results.get(chordKey);
		}
		return results.get(new ArrayList<>(results.keySet()).get(results.size() - 1));

	}

	private ChordNode improveKnucleEstimate(ChordNode knuckle, ChordKey key, ChordKey knuckleKey, int i)
	{
		incrementMessageCount(2);

		if (knuckle.getPredecessor() == null)
			return knuckle;
		Finger finger = knuckle.getPredecessor().getFingerTable().getFinger(Hash.KEY_LENGTH - 1 - i);
		if (finger.getNode() == null)
			return knuckle;
		if (finger.getNode().getNodeKey().isBetween(knuckleKey, key))
			return knuckle;
		else
			return knuckle.getPredecessor();
	}

	@Override
	public HaloChord getChord()
	{
		return (HaloChord) super.getChord();
	}

	// @Override
	// public void join(ChordNode node)
	// {
	// predecessor = null;
	// successorList.clear();
	// successorList.add(this);
	// ChordNode haLocate = Halo.HALocate(node, new ChordKey(this.getNodeId()),
	// haloChord.getHaloRedundancy(),
	// haloChord.getTrustedNodes());
	// successorList.clear();
	// successorList.add(haLocate);
	// }
	//
	// @Override
	// public void fixFingers()
	// {
	// for (int i = 0; i < Hash.KEY_LENGTH; i++)
	// {
	// Finger finger = fingerTable.getFinger(i);
	// ChordKey key = finger.getStart();
	// // finger.setNode(findSuccessor(key));
	// finger.setNode(Halo.HALocate(this, key, haloChord.getHaloRedundancy(),
	// haloChord.getTrustedNodes()));
	// }
	// }

}
