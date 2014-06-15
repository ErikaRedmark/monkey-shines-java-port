package org.erikaredmark.util.collection;

import java.util.Iterator;

/**
 * 
 * Fixed size array that can always store additional items, at the cost of, if full, removing the
 * 'oldest' item from the array.
 * <p/>
 * As old items are removed as needed, this collection only supports adding items and
 * iteration.
 * <p/>
 * This class is not thread safe
 * 
 * @author Erika Redmark
 *
 */
public class RingArray<T> implements Iterable<T> {
	
	// Current index in array.
	private int index;
	
	// actual backing array
	private final T[] array;
	
	/**
	 * 
	 * Creates a new ring array of the given size.
	 * 
	 * @param size
	 * 		size of ring. Must be positive and non-zero
	 * 
	 */
	// We need to use unchecked casts to use backing array
	@SuppressWarnings("unchecked")
	public RingArray(int size) {
		if (size <= 0)  throw new IllegalArgumentException("Size must be positive and non-zero, got: " + size);
		array = (T[]) new Object[size];
		index = 0;
	}
	
	/**
	 * 
	 * Returns the element at the front, or the 'earliest', of the ring. If the ring is
	 * empty this returns {@code null}.
	 * 
	 * @return
	 *		earliest element or {@code null} if ring is empty
	 * 
	 */
	public T front() {
		// index points to INSERTION point
		int oneBefore = index - 1;
		return   oneBefore >= 0
			   ? array[oneBefore]
			   : array[array.length - 1];
	}
	
	/**
	 * 
	 * Returns the element at the back, or the 'latest', of the ring that
	 * hasn't been deleted yet. 
	 * <p/>
	 * If the ring is empty this returns {@code null}
	 * 
	 * @return
	 * 		latest undeleted element or {@code null} if ring is empty
	 * 
	 */
	public T back() {
		// insertion point COULD be back if the value is non-null. That's
		// the best case, and the always case once the ring fills up as it
		// should, so we test it first. If not, since rings ALWAYS start filling
		// at 0, null future entries mean 0 is the oldest.
		final T item = array[index];
		if (item != null)  return item;
		else			   return array[0];
	}
	
	/**
	 * 
	 * Pushes the given object to the front of the array, making it the new 'earliest'
	 * object. If the ring is full, the latest object is bumped out.
	 * 
	 * @param item
	 * 		item to place in ring
	 * 
	 */
	public void pushFront(T item) {
		array[index] = item;
		incrementIndex();
	}
	
	// Increments the index, but if it passes array boundaries, moves it
	// back to position 0
	private void incrementIndex() {
		++index;
		if (index >= array.length) {
			index = 0;
		}
	}

	/**
	 * 
	 * Returns an iterator over this ring array. Iteration starts at the most recent (the first)
	 * in the ring array and keeps going to the last. It is important that the array is not
	 * modified during iteration, as that will cause undefined behaviour.
	 * <p/>
	 * This iterator does not support removal
	 * 
	 */
	@Override public Iterator<T> iterator() {
		int earliestElement = index - 1;
		if (earliestElement < 0)  earliestElement = array.length - 1;
		return new RingIterator(earliestElement);
	}
	
	private final class RingIterator implements Iterator<T> {

		// This DECREMENTS, as going backwards is going from earliest
		// to latest.
		private int curIndex;
		// We start right here. When we get back here iteration is over.
		private final int start;
	
		// Toggled to true when we roll back around
		private boolean done;
		
		// Start must point to the EARLIEST element, not the insertion point.
		private RingIterator(final int start) {
			curIndex = this.start = start;
			// Precheck if this ring buffer is empty. If so, iterator isn't doing anything
			if (array[curIndex] == null)  done = true;
		}
		
		@Override public boolean hasNext() {
			return !(done);
		}

		@Override public T next() {
			int i = curIndex;
			// Decrement curIndex before returning array. Toggle done if,
			// after decrementing, it is back at start. Must do after decrementing
			// or we will stop at start...
			curIndex =   curIndex != 0
					   ? curIndex - 1
					   : array.length - 1;
			
			if (curIndex == start)  done = true;
			// Precheck what is at next element. if it is null, this ring is not full and
			// iteration must stop now
			if (array[curIndex] == null)  done = true;
			
			return array[i];
		}

		@Override public void remove() {
			throw new UnsupportedOperationException("remove not supported on RingIterator");
		}
		
	}
}
