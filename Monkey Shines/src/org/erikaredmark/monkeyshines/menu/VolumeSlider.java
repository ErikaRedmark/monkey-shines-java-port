package org.erikaredmark.monkeyshines.menu;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.erikaredmark.monkeyshines.global.SoundSettings;
import org.erikaredmark.monkeyshines.global.SoundType;
import org.erikaredmark.monkeyshines.global.SoundUtils;
import org.erikaredmark.monkeyshines.resource.SoundManager;

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
				if (!(model.getValueIsAdjusting() ) ) {
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
	}
	
}
