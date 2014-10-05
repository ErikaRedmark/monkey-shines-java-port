package org.erikaredmark.monkeyshines.menu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.erikaredmark.monkeyshines.HighScores;
import org.erikaredmark.monkeyshines.HighScores.HighScore;

/**
 * 
 * Another form of 'menu', albeit much simpler than the main one. Instances are initialised with a 
 * high scores object and will display those high scores, along with a button to go back to the
 * main menu.
 * <p/>
 * This screen is typically reached via either clicking the high scores button, or finishing a world
 * (after the user's high score has been recorded if applicable).
 * 
 * @author Erika Redmark
 *
 */
public final class ViewHighScores extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private final BufferedImage background;
	
	private final HighScores highScores;
	
	/**
	 * 
	 * @param callback
	 * 		when back button is pressed. This is almost always defined to go back to the main menu
	 * 
	 */
	public ViewHighScores(final HighScores highScores, final BackButtonCallback callback) {
		try {
			background = ImageIO.read(ViewHighScores.class.getResourceAsStream("/resources/graphics/mainmenu/highscores/background.png") );
		} catch (IOException e) {
			throw new RuntimeException("Failed to load resources expected in .jar: " + e.getMessage(), e);
		}
		
		this.highScores = highScores;

		setLayout(null);
		
		JButton back = new OkayButton(new ActionListener() { 
			@Override public void actionPerformed(ActionEvent e) { callback.backButtonPressed(); } 
		} );
		
		back.setLocation(546, 434);
		add(back);
		
		
		setSize(640, 480);
		setMinimumSize(new Dimension(640, 480) );
		setPreferredSize(new Dimension(640, 480) );
	}
	
	/**
	 * 
	 * Paint the standard button components for clicking, but everything else can easily just be painted on since they
	 * aren't interactive. This includes the background and the high scores. The actual text of the high score
	 * is not interactive and the locations are explicitly known so there is no point in defining a separate component for it.
	 * 
	 */
	@Override public void paintComponent(Graphics g) {
		g.drawImage(background, 0, 0, null);
		
		List<HighScore> scores = highScores.getHighScores();

		g.setColor(Color.GREEN);
		g.setFont(new Font("sansserif", Font.BOLD, 14) );
		{
			// Keep index number; we need it for drawing purposes.
			int index = 0;
			for (HighScore score : scores) {
				int yPos =  (index * 24) + 128;
				g.drawString(MenuUtils.cutString(score.getName(), 50), 40, yPos);
				g.drawString(String.valueOf(score.getScore() ), 500, yPos);
				++index;
			}
		}
		
		// Do not call; destroys background. Components are still painted regardless.
		// super.paintComponent(g);
	}
	
	public interface BackButtonCallback {
		void backButtonPressed();
	}
	
}
