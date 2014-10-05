package org.erikaredmark.monkeyshines.menu;

import static org.junit.Assert.*;

import org.junit.Test;

public class MenuUtils_Test {

	@Test public void cutStringAlreadySmall() {
		assertEquals("Gordon Freeman", MenuUtils.cutString("Gordon Freeman", 14) );
		assertEquals("Gordon Freeman", MenuUtils.cutString("Gordon Freeman", 15) );
		
	}
	
	@Test public void cutStringTooLarge() {
		assertEquals("United we stand...", MenuUtils.cutString("United we stand, divided we fall", 15) );
	}
	
	@Test public void cutStringLengthCheck() {
		String s = MenuUtils.cutString("1234rt57fuhq42;tyiob0tuyioevhjasjfrthp2qwtbu4faegtriu9th23sf", 26);
		// 26 size + 3 for periods.
		assertEquals(29, s.length() );
	}
	
}
