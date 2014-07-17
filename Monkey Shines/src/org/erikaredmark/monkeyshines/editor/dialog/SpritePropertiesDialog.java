package org.erikaredmark.monkeyshines.editor.dialog;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.SpringLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;

import java.awt.Rectangle;

import javax.swing.JSpinner;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.SpinnerNumberModel;

import org.erikaredmark.monkeyshines.AnimationSpeed;
import org.erikaredmark.monkeyshines.AnimationType;
import org.erikaredmark.monkeyshines.ImmutablePoint2D;
import org.erikaredmark.monkeyshines.ImmutableRectangle;
import org.erikaredmark.monkeyshines.Sprite;
import org.erikaredmark.monkeyshines.Sprite.SpriteType;
import org.erikaredmark.monkeyshines.resource.WorldResource;

public class SpritePropertiesDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private JTextField txtTopLeftX;
	private JTextField txtTopLeftY;
	private JLabel lblWidth;
	private JLabel lblHeight;
	private JTextField txtWidth;
	private JTextField txtHeight;
	private JLabel lblVelocityx;
	private JLabel lblVelocityy;
	private JTextField txtVelocityX;
	private JTextField txtVelocityY;
	private JLabel lblStartX;
	private JLabel lblStartY;
	private JTextField txtStartX;
	private JTextField txtStartY;
	private JSpinner spriteIdSpinner;
	
	private final SpritePropertiesModel model;
	// True if user hits okay, false if otherwise
	private boolean okay = false;
	
	private SpritePropertiesDialog(WorldResource rsrc, SpritePropertiesModel initialModel) {
		model = initialModel;
		
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		JLabel lblTopleftx = new JLabel("Top-Left (X)");
		springLayout.putConstraint(SpringLayout.NORTH, lblTopleftx, 10, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, lblTopleftx, 10, SpringLayout.WEST, this);
		getContentPane().add(lblTopleftx);
		
		JLabel lblToplefty = new JLabel("Top-Left (Y)");
		springLayout.putConstraint(SpringLayout.WEST, lblToplefty, 0, SpringLayout.WEST, lblTopleftx);
		getContentPane().add(lblToplefty);
		
		txtTopLeftX = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, txtTopLeftX, -3, SpringLayout.NORTH, lblTopleftx);
		springLayout.putConstraint(SpringLayout.WEST, txtTopLeftX, 6, SpringLayout.EAST, lblTopleftx);
		getContentPane().add(txtTopLeftX);
		txtTopLeftX.setColumns(10);
		txtTopLeftX.addFocusListener(new FocusListener() {
			@Override public void focusLost(FocusEvent e) {
				model.setSpriteBoundingBoxTopLeftX(Integer.parseInt(txtTopLeftX.getText() ) );
			}
			@Override public void focusGained(FocusEvent e) { }
		});
		
		txtTopLeftY = new JTextField();
		springLayout.putConstraint(SpringLayout.WEST, txtTopLeftY, 6, SpringLayout.EAST, lblToplefty);
		springLayout.putConstraint(SpringLayout.NORTH, lblToplefty, 3, SpringLayout.NORTH, txtTopLeftY);
		springLayout.putConstraint(SpringLayout.NORTH, txtTopLeftY, 10, SpringLayout.SOUTH, txtTopLeftX);
		getContentPane().add(txtTopLeftY);
		txtTopLeftY.setColumns(10);
		txtTopLeftY.addFocusListener(new FocusListener() {
			@Override public void focusLost(FocusEvent e) {
				model.setSpriteBoundingBoxTopLeftY(Integer.parseInt(txtTopLeftY.getText() ) );
			}
			@Override public void focusGained(FocusEvent e) { }
		});
		
		lblWidth = new JLabel("Width");
		springLayout.putConstraint(SpringLayout.NORTH, lblWidth, 0, SpringLayout.NORTH, lblTopleftx);
		springLayout.putConstraint(SpringLayout.WEST, lblWidth, 49, SpringLayout.EAST, txtTopLeftX);
		getContentPane().add(lblWidth);
		
		lblHeight = new JLabel("Height");
		springLayout.putConstraint(SpringLayout.NORTH, lblHeight, 16, SpringLayout.SOUTH, lblWidth);
		springLayout.putConstraint(SpringLayout.EAST, lblHeight, 0, SpringLayout.EAST, lblWidth);
		getContentPane().add(lblHeight);
		
		txtWidth = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, txtWidth, -3, SpringLayout.NORTH, lblTopleftx);
		springLayout.putConstraint(SpringLayout.WEST, txtWidth, 18, SpringLayout.EAST, lblWidth);
		getContentPane().add(txtWidth);
		txtWidth.setColumns(10);
		txtWidth.addFocusListener(new FocusListener() {
			@Override public void focusLost(FocusEvent e) {
				model.setSpriteBoundingBoxWidth(Integer.parseInt(txtWidth.getText() ) );
			}
			@Override public void focusGained(FocusEvent e) { }
		});
		
		txtHeight = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, txtHeight, 10, SpringLayout.SOUTH, txtWidth);
		springLayout.putConstraint(SpringLayout.EAST, txtHeight, 0, SpringLayout.EAST, txtWidth);
		getContentPane().add(txtHeight);
		txtHeight.setColumns(10);
		txtHeight.addFocusListener(new FocusListener() {
			@Override public void focusLost(FocusEvent e) {
				model.setSpriteBoundingBoxHeight(Integer.parseInt(txtHeight.getText() ) );
			}
			@Override public void focusGained(FocusEvent e) { }
		});
		
		lblVelocityx = new JLabel("Velocity (X)");
		springLayout.putConstraint(SpringLayout.NORTH, lblVelocityx, 45, SpringLayout.SOUTH, lblToplefty);
		springLayout.putConstraint(SpringLayout.WEST, lblVelocityx, 0, SpringLayout.WEST, lblTopleftx);
		getContentPane().add(lblVelocityx);
		
		lblVelocityy = new JLabel("Velocity (Y)");
		springLayout.putConstraint(SpringLayout.NORTH, lblVelocityy, 16, SpringLayout.SOUTH, lblVelocityx);
		springLayout.putConstraint(SpringLayout.WEST, lblVelocityy, 0, SpringLayout.WEST, lblTopleftx);
		getContentPane().add(lblVelocityy);
		
		txtVelocityX = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, txtVelocityX, -3, SpringLayout.NORTH, lblVelocityx);
		springLayout.putConstraint(SpringLayout.EAST, txtVelocityX, 0, SpringLayout.EAST, txtTopLeftX);
		getContentPane().add(txtVelocityX);
		txtVelocityX.setColumns(10);
		txtVelocityX.addFocusListener(new FocusListener() {
			@Override public void focusLost(FocusEvent e) {
				model.setSpriteVelocityX(Integer.parseInt(txtVelocityX.getText() ) );
			}
			@Override public void focusGained(FocusEvent e) { }
		});
		
		txtVelocityY = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, txtVelocityY, -3, SpringLayout.NORTH, lblVelocityy);
		springLayout.putConstraint(SpringLayout.EAST, txtVelocityY, 0, SpringLayout.EAST, txtTopLeftX);
		getContentPane().add(txtVelocityY);
		txtVelocityY.setColumns(10);
		txtVelocityY.addFocusListener(new FocusListener() {
			@Override public void focusLost(FocusEvent e) {
				model.setSpriteVelocityY(Integer.parseInt(txtVelocityY.getText() ) );
			}
			@Override public void focusGained(FocusEvent e) { }
		});
		
		lblStartX = new JLabel("Start (X)");
		springLayout.putConstraint(SpringLayout.NORTH, lblStartX, 0, SpringLayout.NORTH, lblVelocityx);
		springLayout.putConstraint(SpringLayout.EAST, lblStartX, 0, SpringLayout.EAST, lblWidth);
		getContentPane().add(lblStartX);
		
		lblStartY = new JLabel("Start (Y)");
		springLayout.putConstraint(SpringLayout.NORTH, lblStartY, 0, SpringLayout.NORTH, lblVelocityy);
		springLayout.putConstraint(SpringLayout.EAST, lblStartY, 0, SpringLayout.EAST, lblWidth);
		getContentPane().add(lblStartY);
		
		txtStartX = new JTextField(String.valueOf(model.getSpriteStartingLocation().x() ) );
		springLayout.putConstraint(SpringLayout.NORTH, txtStartX, 0, SpringLayout.NORTH, txtVelocityX);
		springLayout.putConstraint(SpringLayout.EAST, txtStartX, 0, SpringLayout.EAST, txtWidth);
		getContentPane().add(txtStartX);
		txtStartX.setColumns(10);
		txtStartX.addFocusListener(new FocusListener() {
			@Override public void focusLost(FocusEvent e) {
				model.setSpriteLocationX(Integer.parseInt(txtStartX.getText() ) );
			}
			@Override public void focusGained(FocusEvent e) { }
		});
		
		txtStartY = new JTextField(String.valueOf(model.getSpriteStartingLocation().y() ) );
		springLayout.putConstraint(SpringLayout.NORTH, txtStartY, 0, SpringLayout.NORTH, txtVelocityY);
		springLayout.putConstraint(SpringLayout.WEST, txtStartY, 0, SpringLayout.WEST, txtWidth);
		getContentPane().add(txtStartY);
		txtStartY.setColumns(10);
		txtStartY.addFocusListener(new FocusListener() {
			@Override public void focusLost(FocusEvent e) {
				model.setSpriteLocationY(Integer.parseInt(txtStartY.getText() ) );
			}
			@Override public void focusGained(FocusEvent e) { }
		});
		
		
		final JComboBox<SpriteType> spriteType = new JComboBox<>(SpriteType.values() );
		spriteType.setSelectedItem(model.getSpriteType() );
		spriteType.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent event) {
				final SpriteType type = (SpriteType)spriteType.getSelectedItem();
				model.setSpriteType(type);
			}
		});
		
		springLayout.putConstraint(SpringLayout.NORTH, spriteType, 5, SpringLayout.SOUTH, txtStartY);
		springLayout.putConstraint(SpringLayout.WEST, spriteType, 0, SpringLayout.WEST, lblTopleftx);
		getContentPane().add(spriteType);
		
		final SpriteAnimationCanvas spriteDrawCanvas = new SpriteAnimationCanvas(0, model.getAnimationType(), model.getAnimationSpeed(), rsrc);
		springLayout.putConstraint(SpringLayout.WEST, spriteDrawCanvas, 0, SpringLayout.WEST, txtVelocityY);
		spriteDrawCanvas.setBounds(new Rectangle(0, 0, 40, 40));
		springLayout.putConstraint(SpringLayout.NORTH, spriteDrawCanvas, 20, SpringLayout.SOUTH, spriteType);
		getContentPane().add(spriteDrawCanvas);
		// Listen for changes to sprite id in model. Update graphics accordingly
		model.addPropertyChangeListener(SpritePropertiesModel.PROPERTY_SPRITE_ID, new PropertyChangeListener() {
			@Override public void propertyChange(PropertyChangeEvent e) {
				spriteDrawCanvas.setSpriteId((Integer)e.getNewValue() );
			}
		});
		
		spriteIdSpinner = new JSpinner();
		spriteIdSpinner.setModel(new SpinnerNumberModel(0, 0, rsrc.getSpritesCount() - 1, 1));
		springLayout.putConstraint(SpringLayout.WEST, spriteIdSpinner, 0, SpringLayout.WEST, lblTopleftx);
		springLayout.putConstraint(SpringLayout.SOUTH, spriteIdSpinner, 0, SpringLayout.SOUTH, spriteDrawCanvas);
		springLayout.putConstraint(SpringLayout.EAST, spriteIdSpinner, 0, SpringLayout.EAST, lblTopleftx);
		getContentPane().add(spriteIdSpinner);
		spriteIdSpinner.addChangeListener(new ChangeListener() {
			@Override public void stateChanged(ChangeEvent c) {
				model.setSpriteId((Integer)spriteIdSpinner.getModel().getValue() );
			}
		});
		
		
		JLabel lblId = new JLabel("ID");
		springLayout.putConstraint(SpringLayout.WEST, lblId, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, lblId, -6, SpringLayout.NORTH, spriteIdSpinner);
		getContentPane().add(lblId);
		
		final JComboBox<AnimationType> animationType = new JComboBox<>(AnimationType.values() );
		animationType.setSelectedItem(model.getAnimationType() );
		animationType.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent event) {
				final AnimationType type = (AnimationType)animationType.getSelectedItem();
				model.setAnimationType(type);
				spriteDrawCanvas.setAnimationType(type);
			}
		});
		
		springLayout.putConstraint(SpringLayout.WEST, animationType, 6, SpringLayout.EAST, spriteDrawCanvas);
		springLayout.putConstraint(SpringLayout.SOUTH, animationType, -15, SpringLayout.SOUTH, spriteDrawCanvas);
		springLayout.putConstraint(SpringLayout.EAST, animationType, 4, SpringLayout.WEST, txtWidth);
		getContentPane().add(animationType);
		
		final JComboBox<AnimationSpeed> animationSpeed = new JComboBox<>(AnimationSpeed.values() );
		animationSpeed.setSelectedItem(model.getAnimationSpeed() );
		animationSpeed.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent event) {
				final AnimationSpeed speed = (AnimationSpeed)animationSpeed.getSelectedItem();
				model.setAnimationSpeed(speed);
				spriteDrawCanvas.setAnimationSpeed(speed);
			}
		});
		
		springLayout.putConstraint(SpringLayout.WEST, animationSpeed, 6, SpringLayout.EAST, spriteDrawCanvas);
		springLayout.putConstraint(SpringLayout.SOUTH, animationSpeed, 15, SpringLayout.SOUTH, spriteDrawCanvas);
		springLayout.putConstraint(SpringLayout.EAST, animationSpeed, 4, SpringLayout.WEST, txtWidth);
		getContentPane().add(animationSpeed);
		
		JButton btnNewButton = new JButton("Okay");
		springLayout.putConstraint(SpringLayout.SOUTH, btnNewButton, 0, SpringLayout.SOUTH, spriteDrawCanvas);
		springLayout.putConstraint(SpringLayout.EAST, btnNewButton, 0, SpringLayout.EAST, txtWidth);
		btnNewButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent event) {
				okay = true;
				setVisible(false);
			}
		});
		
		getContentPane().add(btnNewButton);
		
		// Sync the model with the view to create initial population of values.
		spriteIdSpinner.setValue(model.getSpriteId() );
		spriteDrawCanvas.setSpriteId(model.getSpriteId() );
		final ImmutableRectangle initialBoundingBox = model.getSpriteBoundingBox();
		txtTopLeftX.setText(String.valueOf(initialBoundingBox.getLocation().x() ) );
		txtTopLeftY.setText(String.valueOf(initialBoundingBox.getLocation().y() ) );
		txtWidth.setText(String.valueOf(initialBoundingBox.getSize().x() ) );
		txtHeight.setText(String.valueOf(initialBoundingBox.getSize().y() ) );
		
		txtStartX.setText(String.valueOf(model.getSpriteStartingLocation().x() ) );
		txtStartY.setText(String.valueOf(model.getSpriteStartingLocation().y() ) );
		
		txtVelocityX.setText(String.valueOf(model.getSpriteVelocity().x() ) );
		txtVelocityY.setText(String.valueOf(model.getSpriteVelocity().y() ) );
		
		animationType.setSelectedItem(model.getAnimationType() );
		animationSpeed.setSelectedItem(model.getAnimationSpeed() );
	}

	
	/**
	 * 
	 * Blocking method: Launches the dialog and waits for the user to close it. When they do, returns the model object 
	 * backing the view before it closed.
	 * 
	 * @param parent
	 * 
	 * @param rsrc
	 * 		for displaying the sprite graphics in the selector as well as knowing how many sprites are available and setting
	 * 		the bounding constraints for the JSpinner
	 * 
	 * @param startingPoint
	 * 		this is typically where the mouse click was on the editor. The starting X/Y fields will be auto-filled in.
	 * 
	 * @return
	 * 		the model object after the user has finished with the view.
	 * 
	 */
	public static SpritePropertiesModel launch(JComponent parent, WorldResource rsrc, ImmutablePoint2D startingPoint) {
		final SpritePropertiesModel model = SpritePropertiesModel.newModelWithDefaults();
		// Give some intelligent auto-complete based on selection
		model.setSpriteLocationX(startingPoint.x() );
		model.setSpriteLocationY(startingPoint.y() );
		model.setSpriteBoundingBoxTopLeftX(startingPoint.x() );
		model.setSpriteBoundingBoxTopLeftY(startingPoint.y() );
		
		SpritePropertiesDialog dialog = new SpritePropertiesDialog(rsrc, model);
		dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		dialog.setModal(true);
		dialog.setSize(450, 300);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		
		// Dialog is over at this point. Set model based on how it was exited to tell client.
		model.setOkay(dialog.okay);
		
		return model;
	}
	
	/**
	 * 
	 * Blocking method: Launches the dialog and auto fills in the model and view with the values of the selected sprite,
	 * 
	 * @param parent
	 * 
	 * @param rsrc
	 * 		for displaying the sprite graphics in the selector as well as knowing how many sprites are available and setting
	 * 		the bounding constraints for the JSpinner
	 * 
	 * @param sprite
	 * 		the sprite whose properties to use to fill dialog
	 * 
	 * @return
	 */
	public static SpritePropertiesModel launch(JComponent parent, WorldResource rsrc, Sprite sprite) {
		SpritePropertiesModel model = SpritePropertiesModel.fromSprite(sprite);
		SpritePropertiesDialog dialog = new SpritePropertiesDialog(rsrc, model);
		dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		dialog.setModal(true);
		dialog.setSize(450, 300);
		dialog.setLocationRelativeTo(null);
		
		// Show
		dialog.setVisible(true);
		// Return
		dialog.model.setOkay(dialog.okay);
		
		return dialog.model;
	}
}
