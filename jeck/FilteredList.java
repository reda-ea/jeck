package jeck;

import java.util.AbstractSequentialList;
import java.util.List;
import java.util.ListIterator;

/**
 * A filtered, synchronized view on another {@link List}.
 * <p>
 * A {@link FilteredList} is a {@link List} implementation that is based on
 * another List (the base list), but only shows elements satisfying a given
 * condition.
 * <p>
 * It is fully synchronized with the base list, so all operations are
 * "passed through", allowing even modification operations to affect the base
 * list (like any modification to the base list immediately affects the filtered
 * list).
 * <p>
 * In order for elements added (or replaced) to the filtered list (and thus to
 * the base list) to be considered members of the list (so they don't
 * "disappear" after being added), a {@link FilteredList} can modify each
 * element before it is inserted in the list so it complies with the condition,
 * or reject the element if it can't possibly be part of the filtered list (This
 * behavior is not enforced, so a {@link FilteredList} could still choose to
 * accept invalid elements as they are - they just wouldn't show up when reading
 * the list contents).
 * <p>
 * To implement a basic {@link FilteredList}, only the {@link #verify(Object)}
 * method should be implemented (this list would reject any invalid elements).<br>
 * For more control over the inserted elements, the {@link #update(Object)}
 * method should be redefined.
 * 
 * @author Reda El Khattabi
 */
public abstract class FilteredList<E> extends AbstractSequentialList<E>
		implements List<E> {

	private List<E> baseList;

	public FilteredList(List<E> baseList) {
		this.baseList = baseList;
	}

	// ///////////////////////// API

	/**
	 * Checks whether the element can be considered a member of this list
	 */
	protected abstract boolean verify(E element);

	/**
	 * Updates the element before adding it the the base list, so that a
	 * subsequent call to {@link #verify(Object)} would be true.
	 * <p>
	 * The default implementation return valid list elements untouched, and
	 * rejects everything else.
	 * 
	 * @return The modified element (which could be the same) that will be
	 *         actually added to the base list.<br>
	 *         For better consistency, the returned element should be equal to
	 *         the provided element (eg. {@link #contains(Object)} would be true
	 *         for a newly added element)
	 * @throws IllegalArgumentException
	 *             if there is no way to make the element a member of the
	 *             filtered list. The element will not be added to the base
	 *             list.
	 */
	protected E update(E element) throws IllegalArgumentException {
		if (this.verify(element))
			return element;
		throw new IllegalArgumentException();
	}

	// ///////////////////////// LIST

	private class FilteredListIterator implements ListIterator<E> {
		private ListIterator<E> baseIterator;

		private int getInternalIndex(int index)
				throws IndexOutOfBoundsException {
			int inindex = -1;
			int exindex = -1;
			for (E e : FilteredList.this.baseList) {
				++inindex;
				if (FilteredList.this.verify(e))
					if (++exindex == index)
						return inindex;
			}
			if (++exindex == index)
				return inindex + 1;
			throw new IndexOutOfBoundsException();
		}

		public FilteredListIterator(int index) throws IndexOutOfBoundsException {
			this.baseIterator = FilteredList.this.baseList.listIterator(this
					.getInternalIndex(index));
			this.direction = 0;
			this.internalDirection = 0;
		}

		@Override
		public void add(E e) {
			this.baseIterator.add(FilteredList.this.update(e));
			this.direction = 0;
			this.internalDirection = 0;
		}

		@Override
		public boolean hasPrevious() {
			boolean found = false;
			int offset = 0;
			while (this.baseIterator.hasPrevious()) {
				E e = this.baseIterator.previous();
				++offset;
				if (FilteredList.this.verify(e)) {
					found = true;
					break;
				}
			}
			for (int i = 0; i < offset; ++i)
				this.baseIterator.next();
			this.internalDirection = 1;
			return found;
		}

		@Override
		public boolean hasNext() {
			boolean found = false;
			int offset = 0;
			while (this.baseIterator.hasNext()) {
				E e = this.baseIterator.next();
				++offset;
				if (FilteredList.this.verify(e)) {
					found = true;
					break;
				}
			}
			for (int i = 0; i < offset; ++i)
				this.baseIterator.previous();
			this.internalDirection = 1;
			return found;
		}

		@Override
		public E previous() {
			for (;;) {
				E e = this.baseIterator.previous();
				this.internalDirection = -1;
				if (FilteredList.this.verify(e)) {
					this.direction = -1;
					return e;
				}
			}
		}

		@Override
		public E next() {
			for (;;) {
				E e = this.baseIterator.next();
				this.internalDirection = 1;
				if (FilteredList.this.verify(e)) {
					this.direction = 1;
					return e;
				}
			}
		}

		// NOTE only method that reads the base list through index
		@Override
		public int previousIndex() {
			int basePreviousIndex = this.baseIterator.previousIndex();
			int prevIndex = -1;
			for (int i = 0; i <= basePreviousIndex; ++i)
				if (FilteredList.this.verify(FilteredList.this.baseList.get(i)))
					++prevIndex;
			return prevIndex;
		}

		@Override
		public int nextIndex() {
			if (this.hasNext())
				return this.previousIndex() + 1;
			else
				return FilteredList.this.size();
		}

		/**
		 * did we last call next (1), previous (-1) or none (0)
		 */
		private int direction;
		private int internalDirection;

		private void correctDirection() {
			switch (this.direction) {
			case 1:
				if (internalDirection != 1) {
					this.baseIterator.previous();
					this.baseIterator.next();
					this.internalDirection = 1;
				}
				return;
			case -1:
				if (internalDirection != -1) {
					this.baseIterator.next();
					this.baseIterator.previous();
					this.internalDirection = -1;
				}
				return;
			default:
				throw new IllegalStateException();
			}
		}

		@Override
		public void remove() {
			this.correctDirection();
			this.baseIterator.remove();
			this.direction = 0;
			this.internalDirection = 0;

		}

		@Override
		public void set(E e) {
			this.correctDirection();
			this.baseIterator.set(FilteredList.this.update(e));
		}

	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return new FilteredListIterator(index);
	}

	@Override
	public int size() {
		int size = 0;
		for (E e : this.baseList)
			if (this.verify(e))
				++size;
		return size;
	}

}
