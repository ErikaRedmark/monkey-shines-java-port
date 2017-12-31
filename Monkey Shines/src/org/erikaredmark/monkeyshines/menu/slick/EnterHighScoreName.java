package org.erikaredmark.monkeyshines.menu.slick;

import org.newdawn.slick.Color;
import org.newdawn.slick.Font;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.KeyListener;

/**
 * An alternative to AWT for giving a very basic text editor in Slick, shown when a player achieves a high
 * score. Maximum character size is the width of the box. Not intended as a general purpose Slick based text field,
 * and is specific to entering high scores.
 * <p/>
 * This is essentially a bare-bones implementation of a TextField in most GUI toolkits, but simply registers
 * drawable characters to enter into a fixed-size black box positioned somewhere, reads inputs event based from
 * an {@code Input} source until done, and renders the entered text back until Enter is hit in which case the
 * object goes into a 'done' state, and should be removed and the text extracted for additional use on the next
 * update tick.
 * <p/>
 * This has no concept of focus. Whether it is displayed or interpreting user input is up to the main game
 * calling the update/render methods.
 * @author Goddess
 */
// TODO remove this class if Nifty is ever included in the build, and replace it with a Nifty text field.
public class EnterHighScoreName implements KeyListener {
	
	public EnterHighScoreName(int x, int y, Input input, Font font) {
		this.xPos = x;
		this.yPos = y;
		this.input = input;
		this.font = font;
		input.addKeyListener(this);
	}
	
	@Override public void inputEnded() { }
	@Override public void inputStarted() { }
	@Override public void keyReleased(int key, char c) { }
	
	@Override public void keyPressed(int key, char c) { 
		if ((key == Input.KEY_DELETE || key == Input.KEY_BACK) && charPosition > 0) {
			currentText.deleteCharAt(charPosition - 1);
			--charPosition;
		} else if (key == Input.KEY_ENTER || key == Input.KEY_RETURN) {
			setDone();
		} else if ((Character.isAlphabetic(c) || Character.isWhitespace(c)) && charPosition < MAX_CHARACTERS) {
			// visual character, can append.
			currentText.insert(charPosition, c);
			++charPosition;
		}
	}

	@Override public boolean isAcceptingInput() 
		{ return !done; }
	
	
	public void render(Graphics g) {
		g.translate(xPos, yPos);
		g.pushTransform();

		g.setColor(BACKGROUND);
		g.fillRoundRect(0, 0, WIDTH, HEIGHT, 4);
		g.resetFont();
		g.setColor(STATIC_TEXT_FOREGROUND);
		g.setFont(font);
		g.drawString(INTRO_LINE_1, 2, 2);
		g.drawString(INTRO_LINE_2, 2, 18);
		
		g.setColor(TYPE_TEXT_BACKGROUND);
		g.fillRoundRect(2, 38, 296, 20, 2);
		g.setColor(STATIC_TEXT_FOREGROUND);
		// TODO possible efficiency boost caching string each modification
		g.drawString(currentText.toString(), 4, 38);
		
		g.popTransform();
	}
	
	@Override public void setInput(Input input) { }
	
	public String getEnteredText() 
		{ return currentText.toString(); }
	
	/** 
	 * Determines if the enter high score 'dialog' should be considered 'done' as in the user finished
	 * entering their name. If this is true, client should stop calling 'update' and 'render' on this control
	 * and extract the name. 
	 * @return
	 */
	public boolean isDone() { return done; }
	
	private void setDone() {
		done = true;
		input.removeKeyListener(this);
		input.clearKeyPressedRecord();
	}
	
	private boolean done = false;
	private int charPosition = 0;
	private StringBuilder currentText = new StringBuilder(MAX_CHARACTERS);
	
	// Floats so arguments match better for Graphics calls.
	private final float xPos;
	private final float yPos;
	private final Input input;
	private final Font font;
	
	private static final String INTRO_LINE_1 = "Congratulations on a high score!";
	private static final String INTRO_LINE_2 = "Enter your name then hit enter.";
	private static final int WIDTH = 300;
	private static final int HEIGHT = 74;
	private static final Color BACKGROUND = Color.black; 
	private static final Color STATIC_TEXT_FOREGROUND = Color.green;
	private static final Color TYPE_TEXT_BACKGROUND = Color.black.brighter(0.1f);
	private static int MAX_CHARACTERS = 34;
	
}
