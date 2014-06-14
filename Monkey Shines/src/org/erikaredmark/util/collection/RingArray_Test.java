package org.erikaredmark.util.collection;

import org.junit.Test;

public final class RingArray_Test {

	// After initial construction of different sizes, array should be empty
	@Test public void constructionEmpty() {
		// TODO method stub
	}
	
	// Creates ring with size 1 with 1 element. Both front and back should be equal.
	@Test public void oneElementOneSize() {
		// TODO method stub
	}
	
	// Adds a single element to a one size ring and makes sure both front and back
	// return a newly added element (old one should go away
	@Test public void oneSizeOverwrite() {
		// TODO method stub
	}
	
	// Ensures that a non-full ring buffer can still properly get the oldest element.
	// create ring of size 5, add 2 elements, and ensures that back returns the first
	// added, and front returns last added
	@Test public void backFrontWithNullEntries() {
		// TODO method stub
	}
	
	// Creates a ring of size 5 filling it with 5 entries. Back should return oldest and
	// front latest
	@Test public void backFrontWithFull() {
		// TODO method stub
	}
	
	// Creates a ring of size 5 filling it with 6 entries. Back should return the second
	// thing inserted, since the first was overwritten. Front as always should return latest
	@Test public void backFrontWithOverwrite() {
		// TODO method stub
	}
	
}
