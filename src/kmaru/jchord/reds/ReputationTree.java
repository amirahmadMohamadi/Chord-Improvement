package kmaru.jchord.reds;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import kmaru.jchord.ChordKey;

public class ReputationTree
{
	private FingerNode	root;
	int			minimumObservations;
	int			chunkSize;

	ReputationTree(ChordKey rootKey, int chunkSize, int minimumObservations)
	{
		root = new FingerNode(rootKey);
		this.minimumObservations = minimumObservations;
		this.chunkSize = chunkSize;
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

			currentNode.sentLookups += Math.abs(successfulLookups);
			currentNode.successfulLookups += successfulLookups;

			currentNode = nextNode;
		}

		currentNode.sentLookups += Math.abs(successfulLookups);
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
		if (startKey1.equals(key) == false && isFingerSpaceGreaterThanChunkSize(node, i - 1))
			estimatePath(startKey1, key, list);

		return list;
	}

	private boolean isFingerSpaceGreaterThanChunkSize(ChordKey node, int i)
	{
		ChordKey startKey = node.createStartKey(i);
		if (startKey.createStartKey(chunkSize).isBetween(startKey, node.createStartKey(i + 1)))
			return true;
		return false;
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

		while (true)
		{
			if (node.getObservations() >= minimumObservations)
			{
				return node.getScore();
			}
			if (node.isRoot())
				return node.getScore();
			node = node.parent;
		}
	}

	public void printTree(PrintStream stream)
	{
		printTree(stream, root, 0);
	}

	private void printTree(PrintStream stream, FingerNode node, int level)
	{
		for (int i = 0; i < level - 1; i++)
		{
			stream.print("   ");
		}
		if (level > 0)
			stream.print("---");

		stream.println(
				node.key.toString() + "\t:" + node.successfulLookups + "/" + node.sentLookups + "=" + node.getScore());
		for (int i = 0; i < level; i++)
		{
			stream.print("   ");
		}
		if (node.isLeaf())
			return;
		stream.println("|");
		for (FingerNode childNode : node.children)
		{
			printTree(stream, childNode, level + 1);
		}

	}

	public FingerNode getRoot()
	{
		return root;
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

		public boolean isRoot()
		{
			return parent == null;
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
			if (sentLookups < minimumObservations)
				return 0;
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
