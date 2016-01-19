package kmaru.jchord.test;

import java.util.BitSet;

import org.junit.Assert;
import org.junit.Test;

import kmaru.jchord.ChordKey;
import kmaru.jchord.Hash;

public class ChordKeyTest
{

	// @Test
	public void createEndKeyTest()
	{
		Hash.KEY_LENGTH = 160;
		ChordKey key = new ChordKey(new byte[20]);
		key.getKey()[0] = (byte) 255;
		System.out.println(key);
		ChordKey endKey = key.createEndKey(159);
		System.out.println(endKey);
		System.out.println(endKey.createStartKey(159));
		endKey = key.createEndKey(158);
		System.out.println(endKey);
		System.out.println(endKey.createStartKey(158));
		endKey = key.createEndKey(157);
		System.out.println(endKey);
		System.out.println(endKey.createStartKey(157));
		endKey = key.createEndKey(156);
		System.out.println(endKey);
		System.out.println(endKey.createStartKey(156));
	}

//	@Test
	public void distanceTest()
	{
		Hash.KEY_LENGTH = 160;
		ChordKey key = new ChordKey(new byte[20]);
		System.out.println(key);

		ChordKey key2 = new ChordKey(new byte[20]);
		key2.getKey()[19] = 100;
		System.out.println(key2);

		ChordKey dist = key.clockwiseDistance(key2);
		ChordKey dist2 = key2.clockwiseDistance(key);
		System.out.println(dist);
		System.out.println(dist2);

		byte[] reverse = new byte[20];
		for (int j = 0; j < 20; j++)
		{
			byte b = dist2.getKey()[j];
			for (int o = 0; o < 8; o++)
			{
				reverse[19 - j] |= (((b >> o) & 0x1) << (7 - o));
			}
		}

		System.out.println(new ChordKey(reverse));
		int i = BitSet.valueOf(reverse).length();
		System.out.println(i);
		System.out.println(key.createStartKey(i));
		System.out.println(key.createStartKey(i + 1));
		Assert.assertTrue(key2.isBetween(key.createStartKey(i), key.createStartKey(i + 1)));
	}
	
//	@Test
	public void reverseByteTest()
	{
		for (byte i =-128; i< 127 ;i++)
		{
			byte r = 0;
			for (int o = 0; o < 8; o++)
			{
				r |= (((i >> o) & 0x1) << (7 - o));
			}
			
			System.out.println(i + "->" + r);

		}
	}
	
	@Test
	public void distanceFromZeroTest()
	{
		Hash.KEY_LENGTH = 160;
		ChordKey zeroKey = new ChordKey(new byte[20]);
		System.out.println(zeroKey);

		ChordKey key2 = new ChordKey(new byte[20]);
		key2.getKey()[0] = -2;
		key2.getKey()[1] = -126;
		System.out.println(key2);
		
		ChordKey distance = zeroKey.clockwiseDistance(key2);
		System.out.println(distance);
	}
}
