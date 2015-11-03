package kmaru.jchord.reds;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import kmaru.jchord.ChordKey;

public class ReputationTree
{
	FingerNode			root;
	public static int	MINIMUM_OBSERVATIONS = 3;
	public static int	TREE_DEPTH	= 1;

	ReputationTree(ChordKey rootKey)
	{
		root = new FingerNode(rootKey);
	}

	public void addScore(ChordKey key, int successfulLookups)
	{

		List<ChordKey> estimatedPath = estimatePath(root.key, key, new ArrayList<ChordKey>());

		FingerNode currentNode = root;
		FingerNode nextNode;
		for (int i = 0; i < estimatedPath.size(); i++)
		{
			nextNode = null;
			for (FingerNode fingerNode : currentNode.children)
			{
				if (fingerNode.key.equals(estimatedPath.get(i)))
				{
					nextNode = fingerNode;
					break;
				}
			}
			if (nextNode == null)
			{
				nextNode = new FingerNode(estimatedPath.get(i));
				currentNode.addChild(nextNode);
			}

			currentNode.sentLookups += successfulLookups < 0 ? (-1 * successfulLookups) : successfulLookups;
			currentNode.successfulLookups += successfulLookups;

			currentNode = nextNode;
		}

		currentNode.sentLookups += successfulLookups < 0 ? (-1 * successfulLookups) : successfulLookups;
		currentNode.successfulLookups += successfulLookups;
	}

	private List<ChordKey> estimatePath(ChordKey node, ChordKey key, List<ChordKey> list)
	{

		ChordKey distance = key.clockwiseDistance(node);
		int length = distance.getKey().length;
		byte[] reverse = new byte[length];
		for (int j = 0; j < length; j++)
			reverse[length - 1 - j] = distance.getKey()[j];

		int i = BitSet.valueOf(reverse).length();
		if (i == 0)
			return list;

		ChordKey startKey1 = node.createStartKey(i - 1);
		list.add(startKey1);
		if (startKey1.equals(key) == false && list.size() < TREE_DEPTH)
			estimatePath(startKey1, key, list);

		return list;
	}

	public FingerNode findNode(ChordKey key)
	{
		FingerNode currentNode = null;
		FingerNode nextNode = root;

		while (true)
		{
			currentNode = null;
			for (FingerNode node : nextNode.children)
			{
				if (node.key.compareTo(key) <= 0)
					currentNode = node;
				else
					break;
			}
			if (currentNode == null)
				return nextNode;
			if (currentNode.isLeaf())
				return currentNode;
			nextNode = currentNode;
		}
	}

	public double getScore(ChordKey key)
	{
		FingerNode node = findNode(key);

		if (node.equals(root))
			return 0.5;

		while (true)
		{
			if (node.getObservations() >= MINIMUM_OBSERVATIONS)
			{
				return node.getScore();
			}
			if (node.parent.equals(root))
				return node.getScore();
			node = node.parent;
		}
	}

	class FingerNode implements Comparable<FingerNode>
	{
		FingerNode					parent;
		ChordKey					key;
		private List<FingerNode>	children;

		int	sentLookups;
		int	successfulLookups;

		FingerNode(ChordKey key)
		{
			parent = null;
			children = new ArrayList<>();
			this.key = key;
		}

		public boolean isLeaf()
		{
			return children.isEmpty();
		}

		public int getObservations()
		{
			if (isLeaf())
				return sentLookups;

			int observations = 0;
			for (FingerNode fingerNode : children)
			{
				observations += fingerNode.getObservations();
			}

			return observations;
		}

		public double getScore()
		{
			// if (isLeaf())
			// {
			// if (sentLookups == 0)
			// return 0.5;
			// return (double) successfulLookups / sentLookups;
			// }
			// double score = 0;
			// for (FingerNode fingerNode : children)
			// score += fingerNode.getScore();
			// score /= children.size();
			//
			// return score;

			if (sentLookups < MINIMUM_OBSERVATIONS)
				return 0.5;
			return (double) successfulLookups / sentLookups;
		}

		public void addChild(FingerNode node)
		{
			children.add(node);
			Collections.sort(children);
			node.parent = this;
		}

		@Override
		public int compareTo(FingerNode o)
		{
			return key.compareTo(o.key);
		}
	}
}
