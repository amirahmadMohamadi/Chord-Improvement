package kmaru.jchord.halo;

import java.util.Random;
import java.util.Map.Entry;

import kmaru.jchord.ChordKey;
import kmaru.jchord.ChordNode;

public class MaliciousHaloNode extends HaloChordNode
{

	private double	a;
	private Random	random;

	public MaliciousHaloNode(String nodeId, HaloChord haloChord)
	{
		super(nodeId, haloChord);
		this.a = haloChord.getMaliciousBehaviorProbability();
		random = new Random();
	}

	@Override
	protected ChordNode chordLocate(ChordKey key)
	{
		return locate(key);
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

		ChordNode closestMaliciousNode = closestMaliciousNode(key);
		if (closestMaliciousNode != null)
		{
			incrementHopCount(1);
			return closestMaliciousNode;
		}
		return this;
	}

	private ChordNode closestMaliciousNode(ChordKey key)
	{
		ChordKey maliciousKey = null;
		for (Entry<ChordKey, ChordNode> entry : getChord().getSortedNodeMap().entrySet())
		{
			if (entry.getValue() instanceof MaliciousHaloNode == false)
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
				if (entry.getValue() instanceof MaliciousHaloNode == false)
					continue;
				if (entry.getKey().compareTo(key) < 0)
				{
					if (maliciousKey == null)
						maliciousKey = entry.getKey();
					else if (maliciousKey.compareTo(entry.getKey()) > 0)
						maliciousKey = entry.getKey();
				}
			}
		if (maliciousKey == null)
			maliciousKey = getNodeKey();
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
