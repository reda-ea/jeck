package jeck;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * This list will only retreive elements when needed. if {@link #count()} is
 * provided, the list can add elements without fetching all the elements
 * preceding them.
 * 
 * @author Reda El Khattabi
 */
// FIXME loop without requiring size
public abstract class FetchList<T> extends AbstractList<T> {

	// -1 = not counted, -2 = uncountable
	private int tofetch;

	private List<T> data;

	private List<T> fetched;

	private Map<Integer, T> to_add;

	public FetchList() {
		this.tofetch = -1;
		this.data = new ArrayList<T>();
		this.fetched = new ArrayList<T>();
		this.to_add = new HashMap<Integer, T>();
	}

	/**
	 * fetches one element from the actual data source.
	 * 
	 * @return a new element to be added to the list.
	 * @throws NoSuchElementException
	 *             if there are no more elements to fetch
	 * @throws TransactionException
	 *             on error
	 */
	protected abstract T fetch() throws NoSuchElementException;

	/**
	 * counts the number of elements to be fetched.<br>
	 * This is not the actual size of the list (shouldn't take into account
	 * added records) but only that of the "original data".<br>
	 * the list will not attempt to {@link #fetch()} more than this number of
	 * records.<br>
	 * However, if {@link #fetch()} throws a {@link NoSuchElementException}, the
	 * number of fetched records until that point will override this value.<br>
	 * if this method is not supported, the list will fetch all elements when
	 * the size is requested.
	 * 
	 * @throws UnsupportedOperationException
	 *             if we can't count the records without fetching them.
	 * @throws TransactionException
	 *             on error
	 */
	protected abstract int count() throws UnsupportedOperationException;

	/**
	 * fetches one record and adds it to the list.
	 */
	private void fetch_one() throws NoSuchElementException {
		try {
			T t = this.fetch();
			this.fetched.add(t);
			this.data.add(t);
			while (this.to_add.containsKey(this.data.size()))
				this.data.add(this.to_add.remove(this.data.size()));
		} catch (NoSuchElementException e) {
			this.tofetch = this.fetched.size();
			throw e;
		}
	}

	/**
	 * fetches records until we can access element i
	 * 
	 * @throws TransactionException
	 *             , IndexOutOfBoundsException
	 */
	private void fetch_until(int i) throws IndexOutOfBoundsException {
		if (i < 0)
			throw new IndexOutOfBoundsException("negative index");
		while (this.data.size() <= i) {
			try {
				this.fetch_one();
			} catch (NoSuchElementException e) {
				throw new IndexOutOfBoundsException();
			}
		}
	}

	@Override
	public T get(int i) throws IndexOutOfBoundsException {
		for (Map.Entry<Integer, T> e : this.to_add.entrySet())
			if (e.getKey() == i)
				return e.getValue();
		this.fetch_until(i);
		return this.data.get(i);
	}

	@Override
	public int size() {
		if (this.tofetch == -1)
			try {
				this.tofetch = this.count();
			} catch (UnsupportedOperationException e) {
				this.tofetch = -2;
			}
		if (this.tofetch == -2)
			for (;;)
				try {
					this.fetch_one();
				} catch (NoSuchElementException e) {
					break;
				}
		return this.tofetch - this.fetched.size() + this.data.size()
				+ this.to_add.size();
	}

	@Override
	public T set(int i, T e) {
		this.fetch_until(i);
		return this.data.set(i, e);
	}

	@Override
	public void add(int i, T e) {
		if (i < 0)
			throw new IndexOutOfBoundsException();
		try {
			this.data.add(i, e);
		} catch (IndexOutOfBoundsException e1) {
			if (this.tofetch == -1)
				try {
					this.tofetch = this.count();
				} catch (UnsupportedOperationException e2) {
					this.tofetch = -2;
				}
			if (this.tofetch == -2) {
				this.fetch_until(i - 1);
				this.data.add(i, e);
				return;
			}
			if (i > this.size())
				throw new IndexOutOfBoundsException();
			Map<Integer, T> m = new HashMap<Integer, T>();
			for (Map.Entry<Integer, T> t : this.to_add.entrySet())
				m.put(t.getKey() >= i ? t.getKey() + 1 : t.getKey(),
						t.getValue());
			this.to_add = m;
			this.to_add.put(i, e);
		}
	}

	@Override
	public T remove(int i) {
		this.fetch_until(i);
		return this.data.remove(i);
	}

	private static int count_in(List<?> l, Object o) {
		int i = 0;
		for (Object e : l)
			if (e.equals(o))
				++i;
		return i;
	}

	public List<T> get_added() {
		List<T> l = new ArrayList<T>();
		for (T e : this.data) {
			if (l.contains(e))
				continue;
			int c = FetchList.count_in(this.data, e)
					- FetchList.count_in(this.fetched, e);
			for (int i = 0; i < c; ++i)
				l.add(e);
		}
		l.addAll(this.to_add.values());
		return l;
	}

	/**
	 * fetched, undeleted records
	 */
	public List<T> get_original() {
		List<T> l = new ArrayList<T>();
		for (T e : this.data) {
			if (l.contains(e))
				continue;
			int c = Math.min(FetchList.count_in(this.data, e),
					FetchList.count_in(this.fetched, e));
			for (int i = 0; i < c; ++i)
				l.add(e);
		}
		return l;
	}

	public List<T> get_deleted() {
		List<T> l = new ArrayList<T>();
		for (T e : this.fetched) {
			if (l.contains(e))
				continue;
			int c = FetchList.count_in(this.fetched, e)
					- FetchList.count_in(this.data, e);
			for (int i = 0; i < c; ++i)
				l.add(e);
		}
		return l;
	}
}
