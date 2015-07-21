package net.cropsense.bso3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * class representing a key/value dictionary
 * 
 * @author Patrick Kaster
 *
 * @param <K> key
 * @param <V> value
 */
public class KVPairs<K extends Object, V extends Object>
{
	private List<K> keys;
	private List<V> values;

	public KVPairs()
	{
		keys = new ArrayList<K>();
		values = new ArrayList<V>();
	}

	public int size()
	{
		return keys.size();
	}

	public boolean isEmpty()
	{
		return keys.isEmpty();
	}

	public boolean containsKey(Object key)
	{
		return keys.contains(key);
	}

	public boolean containsValue(Object value)
	{
		return values.contains(value);
	}

	public V get(Object key)
	{
		if (!containsKey(key))
			return null;
		else
		{
			return values.get(keys.indexOf(key));
		}
	}

	public void put(K key, V value)
	{
		if (keys.contains(key))
		{
			values.set(keys.indexOf(key), value);
		} else
		{
			keys.add(key);
			values.add(value);
		}
	}

	public void remove(Object key)
	{
		if (keys.contains(key))
		{
			values.remove(keys.indexOf(key));
			keys.remove(key);
		}
	}

	public void putAll(Map<? extends K, ? extends V> m)
	{
		for (K key : m.keySet())
			put(key, m.get(key));
	}

	public void clear()
	{
		keys.clear();
		values.clear();
	}

	public List<K> keys()
	{
		return keys;
	}

	public List<V> values()
	{
		return values;
	}

	@SuppressWarnings("unchecked")
	public boolean equals(Object o)
	{
		if (!(o instanceof KVPairs))
			return false;
		else
		{
			KVPairs<K, V> other = (KVPairs<K, V>) o;
			for (K key : keys)
			{
				if (!(other.containsKey(key)))
					return false;
				else if (!(get(key).equals(other.get(key))))
					return false;
			}
			return true;
		}
	}
}
