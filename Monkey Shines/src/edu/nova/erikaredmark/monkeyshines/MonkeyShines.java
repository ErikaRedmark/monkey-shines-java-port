package edu.nova.erikaredmark.monkeyshines;

import javax.swing.JFrame;

public class MonkeyShines extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4831422227222747908L;
	GameWindow currentWorld;
	KeyboardInput keys;
	
	
	/**
	 * Initialises the game and the GUI.
	 */
	public MonkeyShines() {
		keys = new KeyboardInput();
		currentWorld = new GameWindow(keys);
		// Must add to both.
		this.addKeyListener(keys);
		add(currentWorld);
		setTitle("Monkey Shines");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		setResizable(false);
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new MonkeyShines();
	}

}
