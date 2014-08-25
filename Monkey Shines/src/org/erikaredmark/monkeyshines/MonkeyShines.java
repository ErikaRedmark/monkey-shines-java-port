package org.erikaredmark.monkeyshines;

import org.erikaredmark.monkeyshines.logging.MonkeyShinesLog;
import org.erikaredmark.monkeyshines.menu.MainWindow;

public final class MonkeyShines {
	
	private MonkeyShines() { }

	public static void main(String[] args) {
		MonkeyShinesLog.initialise();
		new MainWindow();
	}

}
