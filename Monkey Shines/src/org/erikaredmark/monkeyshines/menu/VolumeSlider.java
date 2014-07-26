package org.erikaredmark.monkeyshines.menu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSliderUI;

import org.erikaredmark.monkeyshines.global.SoundSettings;
import org.erikaredmark.monkeyshines.global.SoundType;
import org.erikaredmark.monkeyshines.global.SoundUtils;

/**
 * 
 * Represents a volume control that allows the user to choose between 0-100 percent. Basically acts exactly
 * like a slider tool, only graphically skinned to match with Monkey Shines.
 * 
 * @author Erika Redmark
 *
 */
public final class VolumeSlider extends JSlider {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 
	 * Creates a new volume slider controlling the designated volume type (music or sound)
	 * 
	 * @param type
	 * 		the sound type to control.
	 * 
	 * @param demo
	 * 		the sound clip to play on each change. This clip represents an idea of how loud/soft the setting is
	 * 
	 */
	public VolumeSlider(final SoundType type, final Clip demo) {
		super();
		

		final BoundedRangeModel model = new DefaultBoundedRangeModel(SoundSettings.getVolumePercentForType(type), 0, 0, 100) {
			private static final long serialVersionUID = 1L;
			
		};
		
		model.addChangeListener(new ChangeListener() {
			@Override public void stateChanged(ChangeEvent e) {
				type.adjustPercentage(model.getValue() );
				
				// Don't play demo sound unless slider has come to a rest, or that would get annoying real fast.
				if (!(model.getValueIsAdjusting() ) && model.getValue() != 0) {
					float gain = SoundUtils.resolveDecibelOffsetFromPercentage(model.getValue() );
					FloatControl gainControl = (FloatControl) demo.getControl(FloatControl.Type.MASTER_GAIN);
					gainControl.setValue(gain);
					demo.stop();
					demo.setFramePosition(0);
					demo.start();
				}
			}
		});
		
		setModel(model);
		
		setOpaque(false);
		setBorder(null);
		setUI(new MonkeyShinesSliderUI(this) );
		
	}
	
	private static class MonkeyShinesSliderUI extends BasicSliderUI {

		private final BufferedImage SLIDER;
		
		private static final int TRACK_HEIGHT = 17;

	    public MonkeyShinesSliderUI(JSlider slider) {
	        super(slider);
	        
			try {
				SLIDER = ImageIO.read(VolumeSlider.class.getResourceAsStream("/resources/graphics/mainmenu/sound/sliderControl.png") );
			} catch (IOException e) {
				throw new RuntimeException("Bad .jar: Volume slider resource missing or corrupted: " + e.getMessage(), e);
			} 

	    }

	    @Override public void paintTrack(Graphics g) {
	    	// The track is calculated as taking up most of the slider. We only draw TRACK_HEIGHT pixels
	    	int offset = super.trackRect.height - TRACK_HEIGHT;
	    	int drawY =   offset < 0
	    				? super.trackRect.y
	    			    : super.trackRect.y + (offset / 2);
	    	
	        g.setColor(Color.BLACK);
	    	g.drawRect(super.trackRect.x, drawY, 
	    			   super.trackRect.width, TRACK_HEIGHT);
	    	g.drawRect(super.trackRect.x + 1, drawY + 1, 
	    			   super.trackRect.width - 2, TRACK_HEIGHT - 2);
	    	g.setColor(Color.GRAY);
	    	g.fillRect(super.trackRect.x + 2, drawY + 2, 
	    			   super.trackRect.width - 3, TRACK_HEIGHT - 3);
	        
	    }
	    
	    @Override protected Dimension getThumbSize() {
	        return new Dimension(SLIDER.getWidth(), SLIDER.getHeight());
	    }

	    @Override public void paintThumb(Graphics g) {
			g.drawImage(SLIDER, 
						super.thumbRect.x, super.thumbRect.y, 
						super.thumbRect.x + super.thumbRect.width, super.thumbRect.y + super.thumbRect.height, 
						0, 0, 
						SLIDER.getWidth(), SLIDER.getHeight(), 
						null);
	    }

	}
	
}
