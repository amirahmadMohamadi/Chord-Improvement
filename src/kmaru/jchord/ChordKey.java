package kmaru.jchord;

import java.math.BigInteger;
import java.util.Arrays;

public class ChordKey implements Comparable<ChordKey>
{

	String identifier;

	byte[] key;

	public ChordKey(byte[] key)
	{
		this.key = key;
	}

	public ChordKey(String identifier)
	{
		this.identifier = identifier;
		this.key = Hash.hash(identifier);
	}

	public boolean isBetween(ChordKey fromKey, ChordKey toKey)
	{
		if (fromKey.compareTo(toKey) < 0)
		{
			if (this.compareTo(fromKey) > 0 && this.compareTo(toKey) < 0)
			{
				return true;
			}
		}
		else if (fromKey.compareTo(toKey) > 0)
		{
			if (this.compareTo(toKey) < 0 || this.compareTo(fromKey) > 0)
			{
				return true;
			}
		}
		return false;
	}

	public ChordKey createStartKey(int index)
	{
		// byte[] newKey = new byte[key.length];
		// System.arraycopy(key, 0, newKey, 0, key.length);
		// int carry = 0;
		// for (int i = (Hash.KEY_LENGTH - 1) / 8; i >= 0; i--)
		// {
		// int value = key[i] & 0xff;
		// value += (1 << (index % 8)) + carry;
		// newKey[i] = (byte) value;
		// if (value <= 0xff)
		// {
		// break;
		// }
		// carry = (value >> 8) & 0xff;
		// }
		// return new ChordKey(newKey);
		byte[] nodeKey = new byte[key.length+1];
		for (int i = 0; i < key.length; i++)
		{
			nodeKey[i+1] = key[i];
		}

		BigInteger bigInt = new BigInteger(nodeKey);

		StringBuilder sb = new StringBuilder();
		sb.append("1");
		for (int i = 0; i < index; i++)
		{
			sb.append("0");
		}
		bigInt = bigInt.add(new BigInteger(sb.toString(), 2));

		byte[] newKey = new byte[key.length];
		byte[] byteArray = bigInt.toByteArray();
		for (int i = 0; i < byteArray.length && i < newKey.length; i++)
		{
			newKey[key.length - 1 - i] = byteArray[byteArray.length - 1 - i];
		}
		return new ChordKey(newKey);
	}

	public ChordKey createEndKey(int index)
	{
		BigInteger bigInt = new BigInteger(1, key);

		StringBuilder sb = new StringBuilder();
		sb.append("1");
		for (int i = 0; i < index; i++)
		{
			sb.append("0");
		}
		bigInt = bigInt.subtract(new BigInteger(sb.toString(), 2));

		byte[] newKey = new byte[key.length];
		byte[] byteArray = bigInt.toByteArray();
		byte[] byteArrayExtended = new byte[key.length];
		for (int i = 0; i < byteArray.length && i < key.length; i++)
		{
			byteArrayExtended[key.length - 1 - i] = byteArray[byteArray.length - 1 - i];
		}
		for (int i = byteArray.length; i < key.length; i++)
		{
			if (bigInt.signum() == -1)
				byteArrayExtended[i] = (byte) 255;
			else
				byteArrayExtended[i] = (byte) 0;
		}
		for (int i = 0; i < newKey.length; i++)
		{
			newKey[key.length - 1 - i] = byteArrayExtended[byteArrayExtended.length - 1 - i];
		}
		return new ChordKey(newKey);
	}

	@Override
	public int compareTo(ChordKey obj)
	{
		ChordKey targetKey = (ChordKey) obj;
		BigInteger b1 = new BigInteger(1, key);
		BigInteger b2 = new BigInteger(1, targetKey.key);

		return b1.compareTo(b2);
	}

	public ChordKey clockwiseDistance(ChordKey targetKey)
	{
		if (this.compareTo(targetKey) == 0)
			return new ChordKey(new byte[key.length]);

		byte[] newKey = new byte[key.length];
		byte[] byteArrayExtended = new byte[key.length];
		byte[] byteArray = new byte[key.length];
		if (this.compareTo(targetKey) > 0)
		{
			byteArray = new BigInteger(1, key).subtract(new BigInteger(1, targetKey.key)).toByteArray();

		}
		else
		{
			StringBuilder sb = new StringBuilder();
			sb.append("1");
			for (int i = 0; i < key.length * 8; i++)
			{
				sb.append("0");
			}
			byteArray = new BigInteger(sb.toString(), 2).subtract(new BigInteger(1, targetKey.key))
					.add(new BigInteger(1, key)).toByteArray();

		}
		for (int i = 0; i < byteArray.length && i < key.length; i++)
		{
			byteArrayExtended[key.length - 1 - i] = byteArray[byteArray.length - 1 - i];
		}
		for (int i = 0; i < newKey.length; i++)
		{
			newKey[key.length - 1 - i] = byteArrayExtended[byteArrayExtended.length - 1 - i];
		}
		return new ChordKey(newKey);

	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		if (key.length > 4)
		{
			for (int i = 0; i < key.length; i++)
			{
				sb.append(Integer.toString(((int) key[i]) & 0xff) + ".");
			}
		}
		else
		{
			long n = 0;
			for (int i = key.length - 1, j = 0; i >= 0; i--, j++)
			{
				n |= ((key[i] << (8 * j)) & (0xffL << (8 * j)));
			}
			sb.append(Long.toString(n));
		}
		return sb.substring(0, sb.length() - 1).toString();
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public void setIdentifier(String identifier)
	{
		this.identifier = identifier;
	}

	public byte[] getKey()
	{
		return key;
	}

	public void setKey(byte[] key)
	{
		this.key = key;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		result = prime * result + Arrays.hashCode(key);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChordKey other = (ChordKey) obj;
		if (identifier == null)
		{
			if (other.identifier != null)
				return false;
		}
		else if (!identifier.equals(other.identifier))
			return false;
		if (!Arrays.equals(key, other.key))
			return false;
		return true;
	}

}
