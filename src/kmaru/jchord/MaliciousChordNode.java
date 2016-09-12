package kmaru.jchord;

import java.util.Map.Entry;
import java.util.Random;

public class MaliciousChordNode extends ChordNode
{

	private double	a;
	private Random	random;

	public MaliciousChordNode(String nodeId, Chord chord)
	{
		super(nodeId, chord);
		this.a = chord.getMaliciousBehaviorProbability();
		this.random = new Random();
	}

	@Override
	public ChordNode locate(ChordKey key)
	{

		if (a < random.nextDouble())
			return super.locate(key);

		if (this == successorList.get(0))
		{
			return this;
		}

		return closestMaliciousNode(key);
	}

	private ChordNode closestMaliciousNode(ChordKey key)
	{
		ChordKey maliciousKey = null;
		for (Entry<ChordKey, ChordNode> entry : getChord().getSortedNodeMap().entrySet())
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
			for (Entry<ChordKey, ChordNode> entry : getChord().getSortedNodeMap().entrySet())
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
		return getChord().getSortedNodeMap().get(maliciousKey);
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
