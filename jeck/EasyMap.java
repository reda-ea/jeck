package jeck;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Allows creating a map by specifying {@link #keys()}, {@link #get(Object)},
 * {@link #put(Object, Object)} and {@link #remove(Object)}.
 * </p>
 * In addition to that, the map can be customized even more by replacing
 * implemented methods, knowing these simple rules:
 * <ul>
 * <li>{@link #entrySet()} is backed by these 4 operations.</li>
 * <li>{@link #keySet()} and {@link #values()} are backed by {@link #entrySet()}
 * .</li>
 * <li>All other operations are backed by {@link #keySet()}.</li>
 * </ul>
 */
public abstract class EasyMap<K, V> implements Map<K, V> {

	private EasyMapEntrySet<K, V> entry_set;
	private EasyMapKeySet<K, V> key_set;
	private EasyMapValueSet<K, V> value_set;

	public EasyMap() {
		this.entry_set = new EasyMapEntrySet<K, V>(this);
		this.key_set = new EasyMapKeySet<K, V>(this);
		this.value_set = new EasyMapValueSet<K, V>(this);
	}

	/**
	 * Returns a view of the keys contained in this map.<br>
	 * This view is not synchronized with the map, and modifications to it
	 * should have no impact on the map itself (the default implementation will
	 * never attempt to modify it).
	 */
	protected abstract Set<K> keys();

	/**
	 * To avoid concurrent modifications problems, this method will fetch all
	 * keys before removing them.
	 */
	@Override
	public void clear() {
		List<K> keys = new ArrayList<K>(this.keySet());
		for (K e : keys)
			this.remove(e);
	}

	@Override
	public boolean containsKey(Object key) {
		return this.keySet().contains(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return this.values().contains(value);
	}

	/**
	 * <p>
	 * Backed by {@link #keys()}, {@link #get(Object)},
	 * {@link #put(Object, Object)} and {@link #remove(Object)}.
	 * </p>
	 * <p>
	 * Contrary to the {@link Map} recommendation, this {@link Set} does support
	 * the <code>add</code> and <code>addAll</code> operations, and adding an
	 * entry to it will add the corresponding mapping to the table if it does
	 * not exist already. <br>
	 * Adding fails, however, if the key is already mapped (because
	 * {@link Set#add(Object)} is not supposed to replace an existing entry).
	 * </p>
	 */
	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return this.entry_set;
		// return new EasyMapEntrySet<K, V>(this);
	}

	@Override
	public boolean isEmpty() {
		return this.keySet().isEmpty();
	}

	/**
	 * Backed by {@link #entrySet()}.
	 */
	@Override
	public Set<K> keySet() {
		return this.key_set;
		// return new EasyMapKeySet<K, V>(this);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (java.util.Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
			this.put(e.getKey(), e.getValue());
		}
	}

	@Override
	public int size() {
		return this.keySet().size();
	}

	/**
	 * Backed by {@link #entrySet()}.
	 */
	@Override
	public Collection<V> values() {
		return this.value_set;
		// return new EasyMapValueSet<K, V>(this);
	}

	/**
	 * An {@link java.util.Map.Entry} backed by a map.<br>
	 * The entry is initialized with a {@link Map} and a key. Calls to
	 * {@link #getValue()} and {@link #setValue(Object)} are delegated to the
	 * map's {@link Map#get(Object)} and {@link Map#put(Object, Object)}
	 * respectively.<br>
	 * As this entry type is backed by its map, it should not be used with a map
	 * type that is backed by its entries (which would result in an infinite
	 * loop).
	 */
	private static class EasyMapEntry<K, V> implements
			java.util.Map.Entry<K, V> {
		private Map<K, V> map;
		private K key;

		public EasyMapEntry(Map<K, V> map, K key) {
			this.map = map;
			this.key = key;
		}

		@Override
		public K getKey() {
			return this.key;
		}

		@Override
		public V getValue() {
			return this.map.get(this.key);
		}

		@Override
		public V setValue(V value) {
			return this.map.put(this.key, value);
		}

		@Override
		public String toString() {
			return EasyMap.toString(this.getKey()) + "="
					+ EasyMap.toString(this.getValue());
		}
	}

	private static class EasyMapEntryIterator<K, V> implements
			Iterator<java.util.Map.Entry<K, V>> {
		private EasyMap<K, V> map;
		private Iterator<K> key_iter;
		private K current_key;
		private boolean can_remove;

		public EasyMapEntryIterator(EasyMap<K, V> map) {
			this.map = map;
			this.key_iter = this.map.keys().iterator();
			this.current_key = null;
			this.can_remove = false;
		}

		@Override
		public boolean hasNext() {
			return this.key_iter.hasNext();
		}

		@Override
		public java.util.Map.Entry<K, V> next() {
			this.current_key = this.key_iter.next();
			this.can_remove = true;
			return new EasyMap.EasyMapEntry<K, V>(this.map, this.current_key);
		}

		@Override
		public void remove() {
			if (!this.can_remove)
				throw new IllegalStateException();
			this.map.remove(this.current_key);
			this.can_remove = false;
		}

	}

	private static class EasyMapEntrySet<K, V> extends
			AbstractSet<java.util.Map.Entry<K, V>> {
		private EasyMap<K, V> map;

		public EasyMapEntrySet(EasyMap<K, V> map) {
			this.map = map;
		}

		@Override
		public Iterator<java.util.Map.Entry<K, V>> iterator() {
			return new EasyMapEntryIterator<K, V>(this.map);
		}

		@Override
		public int size() {
			return this.map.keys().size();
		}

		/**
		 * Will add a new mapping if possible, but will NEVER replace an old
		 * mapping.<br>
		 * 
		 * @throws IllegalArgumentException
		 *             if the key for this entry already exists in the map.
		 */
		@Override
		public boolean add(java.util.Map.Entry<K, V> e) {
			if (this.map.keys().contains(e.getKey()))
				throw new IllegalArgumentException();
			this.map.put(e.getKey(), e.getValue());
			return true;
		}

	}

	/**
	 * Wrapper around {@link EasyMap#entrySet()#iterator()}
	 */
	private static class EasyMapEntryIteratorWrapper<K, V> {
		private Iterator<java.util.Map.Entry<K, V>> entry_iter;

		public EasyMapEntryIteratorWrapper(EasyMap<K, V> map) {
			this.entry_iter = map.entrySet().iterator();
		}

		public boolean hasNext() {
			return this.entry_iter.hasNext();
		}

		public java.util.Map.Entry<K, V> nextEntry() {
			return this.entry_iter.next();
		}

		public void remove() {
			this.entry_iter.remove();
		}
	}

	private static class EasyMapKeyIterator<K, V> extends
			EasyMapEntryIteratorWrapper<K, V> implements Iterator<K> {

		public EasyMapKeyIterator(EasyMap<K, V> map) {
			super(map);
		}

		@Override
		public K next() {
			return this.nextEntry().getKey();
		}

	}

	private static class EasyMapKeySet<K, V> extends AbstractSet<K> {
		private EasyMap<K, V> map;

		public EasyMapKeySet(EasyMap<K, V> map) {
			this.map = map;
		}

		@Override
		public Iterator<K> iterator() {
			return new EasyMapKeyIterator<K, V>(this.map);
		}

		@Override
		public int size() {
			return this.map.entrySet().size();
		}

	}

	private static class EasyMapValueIterator<K, V> extends
			EasyMapEntryIteratorWrapper<K, V> implements Iterator<V> {

		public EasyMapValueIterator(EasyMap<K, V> map) {
			super(map);
		}

		@Override
		public V next() {
			return this.nextEntry().getValue();
		}

	}

	private static class EasyMapValueSet<K, V> extends AbstractSet<V> {
		private EasyMap<K, V> map;

		public EasyMapValueSet(EasyMap<K, V> map) {
			this.map = map;
		}

		@Override
		public Iterator<V> iterator() {
			return new EasyMapValueIterator<K, V>(map);
		}

		@Override
		public int size() {
			return this.map.entrySet().size();
		}

	}

	public static String toString(Object o) {
		if (o == null)
			return "null";
		return o.toString();
	}

	@Override
	public String toString() {
		String str = "{";
		String sep = "";
		for (Map.Entry<K, V> e : this.entrySet()) {
			str += sep + EasyMap.toString(e);
			sep = ", ";
		}
		return str + "}";
	}
}
