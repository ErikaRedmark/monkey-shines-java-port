package org.erikaredmark.util.collection;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;

public final class RingArray_Test {

	// After initial construction of different sizes, array should be empty
	@Test public void constructionEmpty() {
		RingArray<Integer> ring = new RingArray<>(1);
		RingArray<Integer> ring2 = new RingArray<>(2);
		
		assertEquals(null, ring.front() );
		assertEquals(null, ring.back() );
		
		assertEquals(null, ring2.front() );
		assertEquals(null, ring2.back() );
		
	}
	
	// Creates ring with size 1 with 1 element. Both front and back should be equal.
	@Test public void oneElementOneSize() {
		RingArray<Integer> ring = new RingArray<>(1);
		Integer three = Integer.valueOf(3);
		ring.pushFront(three);
		
		assertEquals(three, ring.front() );
		assertEquals(three, ring.back() );
	}
	
	// Adds two elements to a one size ring and makes sure both front and back
	// return a newly added element (old one should go away)
	@Test public void oneSizeOverwrite() {
		RingArray<Integer> ring = new RingArray<>(1);
		Integer three = Integer.valueOf(3);
		Integer four = Integer.valueOf(4);
		ring.pushFront(three);
		ring.pushFront(four);
		
		assertEquals(four, ring.front() );
		assertEquals(four, ring.back() );
	}
	
	// Ensures that a non-full ring buffer can still properly get the oldest element.
	// create ring of size 5, add 2 elements, and ensures that back returns the first
	// added, and front returns last added
	@Test public void backFrontWithNullEntries() {
		RingArray<Integer> ring = new RingArray<>(5);
		Integer three = Integer.valueOf(3);
		Integer four = Integer.valueOf(4);
		ring.pushFront(three);
		ring.pushFront(four);
		
		assertEquals(four, ring.front() );
		assertEquals(three, ring.back() );
	}
	
	// Creates a ring of size 5 filling it with 5 entries. Back should return oldest and
	// front latest
	@Test public void backFrontWithFull() {
		RingArray<Integer> ring = new RingArray<>(5);
		Integer three = Integer.valueOf(3);
		Integer four = Integer.valueOf(4);
		Integer five = Integer.valueOf(5);
		Integer six = Integer.valueOf(6);
		Integer seven = Integer.valueOf(7);
		ring.pushFront(three);
		ring.pushFront(four);
		ring.pushFront(five);
		ring.pushFront(six);
		ring.pushFront(seven);
		
		assertEquals(seven, ring.front() );
		assertEquals(three, ring.back() );
	}
	
	// Creates a ring of size 5 filling it with 6 entries. Back should return the second
	// thing inserted, since the first was overwritten. Front as always should return latest
	@Test public void backFrontWithOverwrite() {
		RingArray<Integer> ring = new RingArray<>(5);
		Integer three = Integer.valueOf(3);
		Integer four = Integer.valueOf(4);
		Integer five = Integer.valueOf(5);
		Integer six = Integer.valueOf(6);
		Integer seven = Integer.valueOf(7);
		Integer eight = Integer.valueOf(8);
		ring.pushFront(three);
		ring.pushFront(four);
		ring.pushFront(five);
		ring.pushFront(six);
		ring.pushFront(seven);
		ring.pushFront(eight); // overwrites three
		
		assertEquals(eight, ring.front() );
		assertEquals(four, ring.back() );
	}
	
	// Ensures iterator properly works with empty ring array, regardless of size
	@Test public void iterateEmpty() {
		RingArray<Integer> ring = new RingArray<>(2);
		
		Iterator<Integer> it = ring.iterator();
		
		assertFalse(it.hasNext() );
	}
	
	// Ensures iterator returns one entry only
	@Test public void iterateSingle() {
		RingArray<Integer> ring = new RingArray<>(1);
		Integer three = Integer.valueOf(3);
		ring.pushFront(three);
		
		Iterator<Integer> it = ring.iterator();
		
		assertTrue(it.hasNext() );
		assertEquals(three, it.next() );
		
		assertFalse(it.hasNext() );
	}
	
	// Ensures iterator can iterate over a subset of the ring if the ring is not full
	@Test public void iterateMultipleWithNulls() {
		RingArray<Integer> ring = new RingArray<>(4);
		Integer three = Integer.valueOf(3);
		Integer four = Integer.valueOf(4);
		ring.pushFront(three);
		ring.pushFront(four);
		
		Iterator<Integer> it = ring.iterator();
		
		assertTrue(it.hasNext() );
		assertEquals(four, it.next() );
		
		assertTrue(it.hasNext() );
		assertEquals(three, it.next() );
		
		assertFalse(it.hasNext() );
	}
	
	// Ensures iterator can iterate over a full ring
	@Test public void iterateMultipleFull() {
		RingArray<Integer> ring = new RingArray<>(4);
		Integer three = Integer.valueOf(3);
		Integer four = Integer.valueOf(4);
		Integer five = Integer.valueOf(5);
		Integer six = Integer.valueOf(6);
		ring.pushFront(three);
		ring.pushFront(four);
		ring.pushFront(five);
		ring.pushFront(six);
		
		Iterator<Integer> it = ring.iterator();
		assertTrue(it.hasNext() );
		assertEquals(six, it.next() );
		
		assertTrue(it.hasNext() );
		assertEquals(five, it.next() );
		
		assertTrue(it.hasNext() );
		assertEquals(four, it.next() );
		
		assertTrue(it.hasNext() );
		assertEquals(three, it.next() );
		
		assertFalse(it.hasNext() );
	}
	
	// Overwrites some values, placing the internal 'index' in the middle, and ensures iteration order
	// is unaffected.
	@Test public void iterateMultipleFullMiddle() {
		RingArray<Integer> ring = new RingArray<>(4);
		Integer three = Integer.valueOf(3);
		Integer four = Integer.valueOf(4);
		Integer five = Integer.valueOf(5);
		Integer six = Integer.valueOf(6);
		Integer seven = Integer.valueOf(7);
		ring.pushFront(three);
		ring.pushFront(four);
		ring.pushFront(five);
		ring.pushFront(six);
		ring.pushFront(seven); // overwrites three
		
		Iterator<Integer> it = ring.iterator();
		assertTrue(it.hasNext() );
		assertEquals(seven, it.next() );
		
		assertTrue(it.hasNext() );
		assertEquals(six, it.next() );
		
		assertTrue(it.hasNext() );
		assertEquals(five, it.next() );
		
		assertTrue(it.hasNext() );
		assertEquals(four, it.next() );
		
		// three was overwritten
		
		assertFalse(it.hasNext() );
	}
	
	// Ring buffers may not have size zero
	@Test(expected=IllegalArgumentException.class) public void constructionZeroSize() {
		new RingArray<Integer>(0);
	}
	
	// Ring buffers may not have negative size
	@Test(expected=IllegalArgumentException.class) public void constructionNegativeSize() {
		new RingArray<Integer>(-2);
	}
	
}
