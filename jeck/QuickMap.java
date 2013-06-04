package jeck;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class QuickMap<K, V> implements Map<K, V> {

	private Map<K, V> data;

	/**
	 * Easy but unsafe: there's no way to check if elements are of the correct
	 * type.
	 */
	public QuickMap(Map<K, V> map, Object... elements) {
		if (elements.length % 2 != 0)
			throw new IllegalArgumentException(
					"Incomplete data (Odd number of elements)");
		for (int i = 0; i < elements.length; i += 2) {
			@SuppressWarnings("unchecked")
			K k = (K) elements[i];
			@SuppressWarnings("unchecked")
			V v = (V) elements[i + 1];
			this.put(k, v);
		}
	}

	/**
	 * Safest constructor. But we need to manually specify the element types.
	 */
	public QuickMap(Map<K, V> map, Class<K> keyType, Class<V> valueType,
			Object... elements) {
		if (elements.length % 2 != 0)
			throw new IllegalArgumentException(
					"Incomplete data (Odd number of elements)");
		for (int i = 0; i < elements.length; i += 2) {
			if (!keyType.isAssignableFrom(elements[i].getClass()))
				throw new ClassCastException("Element " + i
						+ " must be of type " + keyType.getName());
			if (!valueType.isAssignableFrom(elements[i + 1].getClass()))
				throw new ClassCastException("Element " + i
						+ " must be of type " + valueType.getName());
			this.put(keyType.cast(elements[i]), valueType.cast(elements[i + 1]));
		}
	}

	private static Object[] concat_all(Object o1, Object o2, Object[] o3) {
		Object[] ret = new Object[o3.length + 2];
		System.arraycopy(o3, 0, ret, 2, o3.length);
		return ret;
	}

	/**
	 * Kind of a middle ground, works similar to
	 * {@link #QuickMap(Map, Object...)}, but we are restricted to sub types of
	 * the concrete types of the first 2 elements.
	 */
	public QuickMap(Map<K, V> map, K key1, V value1, Object... elements) {
		this(map, key1.getClass(), value1.getClass(), QuickMap.concat_all(key1,
				value1, elements));
	}

	/**
	 * Same as {@link #QuickMap(Map, Object...)}, using a {@link HashMap}
	 */
	public QuickMap(Object... elements) {
		this(new HashMap<K, V>(), elements);
	}

	/**
	 * Same as {@link #QuickMap(Map, Class, Class, Object...)}, using a
	 * {@link HashMap}
	 */
	public QuickMap(Class<K> keyType, Class<V> valueType, Object... elements) {
		this(new HashMap<K, V>(), keyType, valueType, elements);
	}

	/**
	 * Same as {@link #QuickMap(Map, Object, Object, Object...)}, using a
	 * {@link HashMap}
	 */
	public QuickMap(K key1, V value1, Object... elements) {
		this(new HashMap<K, V>(), key1, value1, elements);
	}

	// TODO Auto-generated delegate
	public void clear() {
		this.data.clear();
	}

	// TODO Auto-generated delegate
	public boolean containsKey(Object key) {
		return this.data.containsKey(key);
	}

	// TODO Auto-generated delegate
	public boolean containsValue(Object value) {
		return this.data.containsValue(value);
	}

	// TODO Auto-generated delegate
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return this.data.entrySet();
	}

	// TODO Auto-generated delegate
	public boolean equals(Object o) {
		return this.data.equals(o);
	}

	// TODO Auto-generated delegate
	public V get(Object key) {
		return this.data.get(key);
	}

	// TODO Auto-generated delegate
	public int hashCode() {
		return this.data.hashCode();
	}

	// TODO Auto-generated delegate
	public boolean isEmpty() {
		return this.data.isEmpty();
	}

	// TODO Auto-generated delegate
	public Set<K> keySet() {
		return this.data.keySet();
	}

	// TODO Auto-generated delegate
	public V put(K key, V value) {
		return this.data.put(key, value);
	}

	// TODO Auto-generated delegate
	public void putAll(Map<? extends K, ? extends V> m) {
		this.data.putAll(m);
	}

	// TODO Auto-generated delegate
	public V remove(Object key) {
		return this.data.remove(key);
	}

	// TODO Auto-generated delegate
	public int size() {
		return this.data.size();
	}

	// TODO Auto-generated delegate
	public Collection<V> values() {
		return this.data.values();
	}

}
