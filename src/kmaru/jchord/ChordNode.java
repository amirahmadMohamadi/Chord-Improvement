package kmaru.jchord;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChordNode
{

	String						nodeId;
	ChordKey					nodeKey;
	protected ChordNode			predecessor;
	protected List<ChordNode>	successorList;
	protected FingerTable		fingerTable;
	List<ChordNode>				fingers;

	private Chord				cord;

	private int					numberOfLocateOperations;

	public int					SUCCESSOR_LIST_SIZE	= 10;

	public ChordNode(String nodeId, Chord chord)
	{
		this.nodeId = nodeId;
		this.nodeKey = new ChordKey(nodeId);
		this.fingerTable = new FingerTable(this);
		numberOfLocateOperations = 0;
		this.create();
		cord = chord;

	}

	/**
	 * Lookup a successor of given identifier
	 *
	 * @param identifier
	 *            an identifier to lookup
	 * @return the successor of given identifier
	 */
	public ChordNode findSuccessor(String identifier)
	{
		ChordKey key = new ChordKey(identifier);
		return findSuccessor(key);
	}

	public ChordNode locate(ChordKey key)
	{
		numberOfLocateOperations++;

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
				incrementMessageCount(1);
				incrementHopCount(1);
				return successorList.get(0).locate(key);
			}
			incrementMessageCount(1);
			incrementHopCount(1);
			return node.locate(key);
		}

	}

	/**
	 * Lookup a successor of given key
	 *
	 * @param identifier
	 *            an identifier to lookup
	 * @return the successor of given identifier
	 */
	public ChordNode findSuccessor(ChordKey key)
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
				return successorList.get(0).findSuccessor(key);
			}
			return node.findSuccessor(key);
		}
	}

	protected ChordNode closestPrecedingNode(ChordKey key)
	{
		ChordNode tempNode = null;
		for (ChordNode successor : successorList)
		{
			if (successor.getNodeKey().isBetween(getNodeKey(), key))
			{
				tempNode = successor;
				break;
			}
		}

		if (tempNode == null)
			tempNode = this;

		for (int i = Hash.KEY_LENGTH - 1; i >= 0; i--)
		{
			Finger finger = fingerTable.getFinger(i);
			ChordKey fingerKey = finger.getNode().getNodeKey();
			if (fingerKey.isBetween(tempNode.getNodeKey(), key))
			{
				return finger.getNode();
			}
		}
		return tempNode;
	}

	/**
	 * Creates a new Chord ring.
	 */
	public void create()
	{
		predecessor = null;
		successorList = new ArrayList<>();
		successorList.add(this);
	}

	public void dispose()
	{
		if (getSuccessorList() != null)
		{
			getSuccessorList().clear();
			successorList = null;
		}
		this.predecessor = null;
		this.fingers.clear();
		this.fingerTable.fingers = null;

	}

	public void leave()
	{
		successorList.get(0).predecessorRemoved();
		if (getPredecessor() != null)
			getPredecessor().successorRemved(this);
	}

	private void successorRemved(ChordNode node)
	{
		if (successorList.contains(node))
		{
			successorList.remove(node);
			successorList.addAll(successorList.get(successorList.size() - 1).successorList);
			while (successorList.size() > SUCCESSOR_LIST_SIZE)
				successorList.remove(successorList.size() - 1);

			if (getPredecessor() != null && getPredecessor() != this)
				getPredecessor().successorRemved(node);
		}
	}

	private void predecessorRemoved()
	{
		predecessor = predecessor.getPredecessor();
	}

	/**
	 * Joins a Chord ring with a node in the Chord ring
	 *
	 * @param node
	 *            a bootstrapping node
	 */
	public void join(ChordNode node)
	{
		predecessor = null;
		successorList.clear();
		successorList.add(node.findSuccessor(this.getNodeId()));
	}

	/**
	 * Verifies the successor, and tells the successor about this node. Should be called periodically.
	 */
	public void stabilize()
	{
		ChordNode node = successorList.get(0).getPredecessor();
		if (node != null)
		{
			ChordKey key = node.getNodeKey();
			if ((this == successorList.get(0)) || key.isBetween(this.getNodeKey(), successorList.get(0).getNodeKey()))
			{
				successorList.clear();
				successorList.add(node);
			}
		}
		successorList.addAll(successorList.get(0).successorList);
		while (successorList.size() > SUCCESSOR_LIST_SIZE)
			successorList.remove(successorList.size() - 1);
		successorList.get(0).notifyPredecessor(this);

	}

	private void notifyPredecessor(ChordNode node)
	{
		ChordKey key = node.getNodeKey();
		if (predecessor == null || key.isBetween(predecessor.getNodeKey(), this.getNodeKey()))
		{
			predecessor = node;
		}
	}

	/**
	 * Refreshes finger table entries.
	 */
	public void fixFingers()
	{
		for (int i = 0; i < Hash.KEY_LENGTH; i++)
		{
			Finger finger = fingerTable.getFinger(i);
			ChordKey key = finger.getStart();
			finger.setNode(findSuccessor(key));
		}

		Set<ChordNode> fingerSet = new HashSet<>();
		for (int j = 0; j < Hash.KEY_LENGTH; j++)
			fingerSet.add(getFingerTable().getFinger(j).getNode());
		fingers = new ArrayList<>(fingerSet);
	}

	public void fixSuccessorList()
	{
		ChordNode chordNode = null;
		for (int i = 0; i < successorList.size() - 1; i++)
		{
			chordNode = successorList.get(i);
			if (chordNode.getSuccessor().equals(successorList.get(i + 1)) == false)
				break;
		}

		if (chordNode != null)
		{
			successorList.subList(successorList.indexOf(chordNode) + 1, successorList.size()).clear();
			;
			successorList.add(chordNode.getSuccessor());
			successorList.addAll(chordNode.getSuccessor().successorList);
			while (successorList.size() > SUCCESSOR_LIST_SIZE)
				successorList.remove(successorList.size() - 1);

		}
	}

	public void validateSuccessorList()
	{
		if (successorList.get(0).getPredecessor().equals(this) == false)
			throw new IllegalArgumentException("Successor is incorrect!");

		for (int i = 0; i < successorList.size() - 1; i++)
		{
			ChordNode chordNode = successorList.get(i);
			if (chordNode.getSuccessor().equals(successorList.get(i + 1)) == false)
				throw new IllegalArgumentException("Successor list is incorrect!");
		}
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("ChordNode[");
		sb.append("ID=" + nodeId);
		sb.append(",KEY=" + nodeKey);
		sb.append("]");
		return sb.toString();
	}

	public void printFingerTable(PrintStream out)
	{
		out.println("=======================================================");
		out.println("FingerTable: " + this);
		out.println("-------------------------------------------------------");
		out.println("Predecessor: " + predecessor);
		out.println("Successor: " + successorList.get(0));
		out.println("-------------------------------------------------------");
		for (int i = 0; i < Hash.KEY_LENGTH; i++)
		{
			Finger finger = fingerTable.getFinger(i);
			out.println(finger.getStart() + "\t" + finger.getNode());
		}
		out.println("=======================================================");
	}

	public Chord getChord()
	{
		return cord;
	}

	public String getNodeId()
	{
		return nodeId;
	}

	public void setNodeId(String nodeId)
	{
		this.nodeId = nodeId;
	}

	public ChordKey getNodeKey()
	{
		return nodeKey;
	}

	public void setNodeKey(ChordKey nodeKey)
	{
		this.nodeKey = nodeKey;
	}

	public ChordNode getPredecessor()
	{
		return predecessor;
	}

	public void setPredecessor(ChordNode predecessor)
	{
		this.predecessor = predecessor;
	}

	public ChordNode getSuccessor()
	{
		return successorList.get(0);
	}

	public List<ChordNode> getSuccessorList()
	{
		return successorList;
	}

	public void setSuccessor(ChordNode successor)
	{
		this.successorList.clear();
		this.successorList.add(successor);
		successorList.addAll(successorList.get(0).successorList);
		while (successorList.size() > SUCCESSOR_LIST_SIZE)
			successorList.remove(successorList.size() - 1);
	}

	public FingerTable getFingerTable()
	{
		return fingerTable;
	}

	public void setFingerTable(FingerTable fingerTable)
	{
		this.fingerTable = fingerTable;
	}

	public boolean isMalicious()
	{
		return false;
	}

	public boolean hasFinger(ChordNode node)
	{
		for (int i = 0; i < Hash.KEY_LENGTH; i++)
			if (getFingerTable().getFinger(i).getNode().equals(node))
				return true;
		return false;
	}

	public ChordNode getFingerNode(int i)
	{
		if (i < fingers.size())
			return fingers.get(i);
		return fingers.get(0);
	}

	public List<ChordNode> getFingerList()
	{
		return this.fingers;
	}

	public static boolean validateResult(ChordNode resultNode, ChordKey key)
	{
		if (resultNode.getPredecessor() == null)
			return false;
		return key.isBetween(resultNode.getPredecessor().getNodeKey(), resultNode.getNodeKey())
				|| resultNode.getNodeKey().equals(key);
	}

	public int getNumberOfLocateOperations()
	{
		return numberOfLocateOperations;
	}

	public void incrementMessageCount(int number)
	{
		int messageCount = (int) getChord().getSimulationData().getCustomProperties().get("Message count");
		getChord().getSimulationData().getCustomProperties().put("Message count", messageCount + number);
	}

	public void insertNewHopCount()
	{
		@SuppressWarnings("unchecked")
		List<Integer> list = (List<Integer>) getChord().getSimulationData().getCustomProperties().get("Hop count");
		list.add(0);
		getChord().getSimulationData().getCustomProperties().put("Hop count", list);
	}

	public void incrementHopCount(int number)
	{
		@SuppressWarnings("unchecked")
		List<Integer> list = (List<Integer>) getChord().getSimulationData().getCustomProperties().get("Hop count");
		if (list.isEmpty())
			return;
		int hopcount = list.get(list.size() - 1);
		hopcount = hopcount + number;
		list.set(list.size() - 1, hopcount);
		getChord().getSimulationData().getCustomProperties().put("Hop count", list);
	}

}
