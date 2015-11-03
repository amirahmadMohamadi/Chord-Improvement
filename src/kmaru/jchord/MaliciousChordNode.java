package kmaru.jchord;

import java.util.Map.Entry;

public class MaliciousChordNode extends ChordNode
{

	Chord	chord;

	public MaliciousChordNode(String nodeId, Chord chord)
	{
		super(nodeId);
		this.chord = chord;
	}

	
	
	@Override
	public ChordNode locate(ChordKey key)
	{
		if (this == successorList.get(0))
		{
			return this;
		}

		return closestMaliciousNode(key);
	}

	private ChordNode closestMaliciousNode(ChordKey key)
	{
		ChordKey maliciousKey = null;
		for (Entry<ChordKey, ChordNode> entry : chord.getSortedNodeMap().entrySet())
		{
			if (entry.getValue() instanceof MaliciousChordNode == false)
				continue;
			if (entry.getKey().compareTo(key) > 0)
			{
				if (maliciousKey == null)
					maliciousKey = entry.getKey();
				else if (maliciousKey.compareTo(entry.getKey()) > 0)
					maliciousKey = entry.getKey();
			}
		}
		if (maliciousKey == null)
			for (Entry<ChordKey, ChordNode> entry : chord.getSortedNodeMap().entrySet())
			{
				if (entry.getValue() instanceof MaliciousChordNode == false)
					continue;
				if (entry.getKey().compareTo(key) < 0)
				{
					if (maliciousKey == null)
						maliciousKey = entry.getKey();
					else if (maliciousKey.compareTo(entry.getKey()) > 0)
						maliciousKey = entry.getKey();
				}
			}
		return chord.getSortedNodeMap().get(maliciousKey);
	}

	@Override
	public String toString()
	{
		return "Malicious " + super.toString();
	}

	@Override
	public boolean isMalicious()
	{
		return true;
	}

	
}
