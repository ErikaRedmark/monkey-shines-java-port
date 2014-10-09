package org.erikaredmark.monkeyshines.editor.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.erikaredmark.monkeyshines.DeathAnimation;
import org.erikaredmark.monkeyshines.Hazard;
import org.erikaredmark.monkeyshines.editor.HazardMutable;
import org.erikaredmark.monkeyshines.resource.WorldResource;

/**
 * 
 * For a given graphics resource which supplies a given number of hazard icons, allows the user to define each hazard
 * icon graphically in terms of what properties each hazard has when placed on a world. This is not WHERE the hazards are
 * placed, but rather what they actually are.
 * <p/>
 * Initially all hazards start out at some default value. Since hazard sprite sheets enumerate tile sized hazards and each 
 * column is a new hazard, the number of hazard available to the user is equal to the length-wise size of the hazard sheet 
 * divided by the pixels per tile (length-wise). Since these dialogs can only be launched with valid graphics resources users
 * cannot assign hazards to non-existent sprites.
 * 
 * @author Erika Redmark
 *
 */
public class EditHazardsDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private static final String CLASS_NAME = "org.erikaredmark.monkeyshines.editor.dialog.EditHazardsDialog";
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
	
	
	private final EditHazardsModel model;
	private final DefaultListModel<Hazard> hazardListModel;
	private final EditSingleHazardPanel editPanel;
	private final JList<Hazard> hazardList;
	
	/**
	 * 
	 * Constructs the dialog with the diven model. The dialog is ready but not immediately visible after this construction.
	 * 
	 * @param model
	 * 
	 * @param rsrc
	 * 
	 */
	private EditHazardsDialog(final EditHazardsModel model, final WorldResource rsrc) {
		this.model = model;
		getContentPane().setLayout(new GridBagLayout() );
		
		

		/* ------------------ Hazard list ------------------- */
		// This list will sync changes to the underlying EditHazardsModel
		hazardListModel = new DefaultListModel<>();
		
		// Assumption: All elements in the passed model are sorted, since they can only be actually generated from here,
		// and all elements generated here will be sorted
		for (Hazard h : this.model.getHazards() ) {
			hazardListModel.addElement(h);
		}
		
		hazardList = new JList<Hazard>(hazardListModel);
		hazardList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		hazardList.setCellRenderer(new HazardCellRenderer(rsrc) );
		// Attach to upper left of window and extend to bottom
		
		final GridBagConstraints hazardListGbc = new GridBagConstraints();
		hazardListGbc.gridx = 0;
		hazardListGbc.gridy = 0;
		hazardListGbc.weightx = 1;
		hazardListGbc.weighty = 1;
		hazardListGbc.gridheight = GridBagConstraints.REMAINDER;
		hazardListGbc.gridwidth = 1;
		hazardListGbc.fill = GridBagConstraints.BOTH;
		// Embed list in Scrollable pane to allow scrollbars, but apply constraints to scrollable pane
		JScrollPane hazardListWrapped = new JScrollPane(hazardList);
		getContentPane().add(hazardListWrapped, hazardListGbc);
		
		// Define edit panel here so listener for delete hazard can access it now
		editPanel = new EditSingleHazardPanel();
		
		
		/* -------------------- Edit Hazard Panel --------------------- */
		
		/* ----------------- Selection Listener ----------------- */
		/* Selected hazards are under edit in the edit panel.	  */
		/* This listener binds to the original hazard list.		  */
		hazardList.addListSelectionListener(new ListSelectionListener() {
			@Override public void valueChanged(ListSelectionEvent event) {
				// Does not save the current hazard. Saving should be done on each property change.
				Hazard newHazard = hazardList.getSelectedValue();
				if (newHazard == null)  return;
				
				editPanel.setHazardEditing(newHazard);
			}
		});
		
		final GridBagConstraints editPanelGbc = new GridBagConstraints();
		editPanelGbc.gridx = 2;
		editPanelGbc.gridy = 1;
		editPanelGbc.gridwidth = GridBagConstraints.REMAINDER;
		editPanelGbc.gridheight = GridBagConstraints.REMAINDER;
		getContentPane().add(editPanel, editPanelGbc);
		
		hazardList.setVisible(true);
		setSize(600, 300);
	}
	
	private void save() {
		if (this.editPanel.isEditingHazard() ) {
			if (!(this.editPanel.isHazardEdited() ) )  return;

			// Save the current selection so we can re-select when we redo the list model.
			int currentSelection = hazardList.getSelectedIndex();
			
			Hazard editedCopy = this.editPanel.getHazardEdited();
			List<Hazard> newModel = model.getMutableHazards();
			
			Hazard.replaceHazard(newModel, editedCopy);
			
			// update the list model used by the view
			this.hazardListModel.clear();
			for (Hazard h : newModel) {
				this.hazardListModel.addElement(h);
			}
			
			hazardList.setSelectedIndex(currentSelection);
		}
	}
	
	/**
	 * 
	 * Panel used by dialog that, given a specific Hazard property, alonws one to make modifications to it. Changes to
	 * a hazard can then replace a hazard in the view, and therefore the main model, or can be discarded.
	 * 
	 * @author Erika Redmark
	 *
	 */
	private final class EditSingleHazardPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		
		// This mutable copy is never published outside of this object.
		private HazardMutable currentlyEditingHazard;
		
		// The immutable id of even the mutable hazard can tell us what to replace, but for easeness we
		// store a reference to the immutable hazard this one is derived to do equals calculations within
		// this object (for unsaved changes and such)
		private Hazard originalFromCurrentEdit;
		
		final JCheckBox explodesBtn;
		final JComboBox<DeathAnimation> deathAnimation;
		
		private EditSingleHazardPanel() {
			/* ------------- Function ------------- */
			final JLabel deathAnimationLbl = new JLabel("Death Animation");
			deathAnimation = new JComboBox<>(new DefaultComboBoxModel<>(DeathAnimation.values() ) );
			deathAnimation.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent evt) {
					currentlyEditingHazard.setDeathAnimation((DeathAnimation)deathAnimation.getSelectedItem() );
					EditHazardsDialog.this.save();
				}
				
			});
			
			explodesBtn = new JCheckBox(new AbstractAction("Explodes") {
				private static final long serialVersionUID = 1L;

				@Override public void actionPerformed(ActionEvent evt) {
					// set mutable hazard to same exploded state as checkbox
					currentlyEditingHazard.setExplodes(((JCheckBox)evt.getSource()).isSelected() );
					EditHazardsDialog.this.save();
				}
				
			});
			
			/* --------------- Form ---------------- */
			setLayout(new GridBagLayout() );
			
			final GridBagConstraints deathAnimationLblGbc = new GridBagConstraints();
			deathAnimationLblGbc.gridx = 1;
			deathAnimationLblGbc.gridy = 1;
			add(deathAnimationLbl, deathAnimationLblGbc);
			
			final GridBagConstraints deathAnimationGbc = new GridBagConstraints();
			deathAnimationGbc.gridx = 2;
			deathAnimationGbc.gridy = 1;
			add(deathAnimation, deathAnimationGbc);
			
			final GridBagConstraints explodesGbc = new GridBagConstraints();
			explodesGbc.gridx = 1;
			explodesGbc.gridy = 2;
			explodesGbc.gridwidth = GridBagConstraints.REMAINDER;
			add(explodesBtn, explodesGbc);
			
			// Initially, there are no hazards being edited on construction (we have no initial selection)
			noHazardEditing();
		}
		
		
		/**
		 * 
		 * Indicates to this panel no hazard is currently selected for editing.
		 * 
		 */
		public void noHazardEditing() {
			explodesBtn.setEnabled(false);
			deathAnimation.setEnabled(false);
		}
		
		/**
		 * 
		 * Makes a mutable copy of the given hazard and places it under editing.
		 * <p/>
		 * This also enables the view if it is disabled and updates it to the properties of the hazard
		 * 
		 */
		public void setHazardEditing(final Hazard hazard) {
			this.currentlyEditingHazard = hazard.mutableCopy();
			this.originalFromCurrentEdit = hazard;
			
			explodesBtn.setEnabled(true);
			deathAnimation.setEnabled(true);
			
			explodesBtn.setSelected(originalFromCurrentEdit.getExplodes() );
			deathAnimation.setSelectedItem(originalFromCurrentEdit.getDeathAnimation() );
		}
		
		/**
		 * 
		 * Returns an immutable copy of the hazard currently under edit. This call MAY fail with an exception if there is
		 * no hazard under edit. Use {@code isEditingHazard() } first to confirm.
		 * 
		 * @throws IllegalStateException
		 * 		if called when there is no hazard being edited
		 * 
		 */
		public Hazard getHazardEdited() {
			if (currentlyEditingHazard == null)  throw new IllegalStateException("No hazard being editing");
			return this.currentlyEditingHazard.immutableCopy();
		}
		
		/**
		 * 
		 * Determines if the given hazard being edited by this panel is actually different in some way from the hazard that
		 * it started with (i.e have any changes been made). This method may also throw an exception if there is no hazard
		 * under edit. Use {@code isEditingHazard() } first to confirm.
		 * 
		 * @return
		 * 		{@code true} if the hazard is changed, {@code false} if the hazard is the same
		 * 
		 * @throws IllegalStateException
		 * 		if called when there is no hazard being edited
		 */
		public boolean isHazardEdited() {
			if (currentlyEditingHazard == null)  throw new IllegalStateException("No hazard being editing");
			// Don't compare Id or resources. Those never change
			return    currentlyEditingHazard.getDeathAnimation() != originalFromCurrentEdit.getDeathAnimation()
				   || currentlyEditingHazard.getExplodes() != originalFromCurrentEdit.getExplodes();
		}
		
		/**
		 * 
		 * Determines if this panel is currently editing a hazard or if it is not.
		 * 
		 * @return
		 * 		{@code true} if editing a hazard, {@code false} if otherwise
		 * 
		 */
		public boolean isEditingHazard() {
			return currentlyEditingHazard != null;
		}
		
		
	}
	
	/**
	 * 
	 * Allows rendering of the Image of the hazard, whether or not it explodes, and the type of
	 * death animation (in that order) in 20x20 chunks with 2px padding in-between each.
	 * <p/>
	 * The graphic used for the hazard is taken from the index in the list (index in list = rsrc
	 * index). The graphics used for the other properties are internal to the .jar and are taken if
	 * the data model requires them.
	 * 
	 * @author Erika Redmark
	 *
	 */
	private static final class HazardCellRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 1L;
		
		private final Map<DeathAnimation, ImageIcon> deathTypeIcons = new HashMap<>();
		private ImageIcon explodesIcon;
		private final List<ImageIcon> indexToHazard = new ArrayList<>();
		
		private HazardCellRenderer(final WorldResource rsrc) {
			try {
				// All or none. If any one load fails, all image icons will not be initialised.
				BufferedImage explodes = ImageIO.read(EditHazardsDialog.class.getResourceAsStream("/resources/graphics/editor/hazard/explodesIcon.png") );
				BufferedImage burn = ImageIO.read(EditHazardsDialog.class.getResourceAsStream("/resources/graphics/editor/hazard/burnIcon.png") );
				BufferedImage bee = ImageIO.read(EditHazardsDialog.class.getResourceAsStream("/resources/graphics/editor/hazard/beeSting.png") );
				BufferedImage standardDeath = ImageIO.read(EditHazardsDialog.class.getResourceAsStream("/resources/graphics/editor/hazard/standardDeath.png") );
				BufferedImage electricDeath = ImageIO.read(EditHazardsDialog.class.getResourceAsStream("/resources/graphics/editor/hazard/electricDeath.png") );
				
				deathTypeIcons.put(DeathAnimation.BURN, new ImageIcon(burn) );
				deathTypeIcons.put(DeathAnimation.BEE, new ImageIcon(bee) );
				deathTypeIcons.put(DeathAnimation.NORMAL, new ImageIcon(standardDeath) );
				deathTypeIcons.put(DeathAnimation.ELECTRIC, new ImageIcon(electricDeath) );
				
				explodesIcon = new ImageIcon(explodes);
				
			} catch (IOException e) {
				// No big deal, we just can't render the images like we wanted.
				LOGGER.log(Level.SEVERE,
						   "Missing graphics resources in .jar; cannot render information images in hazards display: " + e.getMessage(),
						   e);
			}
			
			// indexToHazard ALWAYS works.
			BufferedImage source = rsrc.getHazardSheet();
			for (int srcX = 0; srcX < source.getWidth(); srcX += 20) {
				BufferedImage destination = new BufferedImage(20, 20, source.getType() );
				Graphics2D g2d = destination.createGraphics();
				try {
					g2d.drawImage(source, 0, 0, 20, 20, srcX, 0, srcX + 20, 20, null);
					indexToHazard.add(new ImageIcon(destination) ); 
				} finally {
					g2d.dispose();
				}
			}
		}
		
		@Override public Component getListCellRendererComponent(JList<?> list, 
																Object v, 
																int index,
																boolean isSelected, 
																boolean cellHasFocus) {
			
			assert v instanceof Hazard;
			Hazard value = (Hazard) v;
			
			// Return a composite of three labels, each showing one image based on either the id,
			// explosion type, or death type of the hazard
			JPanel display = new JPanel();
			display.setLayout(new FlowLayout(FlowLayout.LEFT) );
			
			JLabel hazardItself = new JLabel();
			hazardItself.setIcon(indexToHazard.get(index) );
			display.add(hazardItself);
			
			// Null is acceptable for the next two
			JLabel deathType = new JLabel();
			deathType.setIcon(deathTypeIcons.get(value.getDeathAnimation() ) );
			display.add(deathType);
			
			if (value.explodes() && explodesIcon != null ) {
				JLabel explod = new JLabel();
				explod.setIcon(explodesIcon);
				display.add(explod);
			}
			
			if (isSelected) {
				display.setBackground(Color.GRAY);
			}
			
			return display;
		}
	}

	/**
	 * 
	 * Launches the given dialog with the associated parent and world resource. This call will block until the user is
	 * finished, upon which the model will be returned representing the logical state that the user made changes to.
	 * This model should then be synced with the world resource currently being edited for changes to take effect.
	 * 
	 * @param parent
	 * 		the parent component
	 * 
	 * @param rsrc
	 * 		the world resource to draw the graphics from. <strong> This affects how many hazards are available</strong>
	 * 		based on the size of the sprite sheet.
	 * 
	 */
	public static EditHazardsModel launch(JComponent parent, WorldResource rsrc, List<Hazard> worldHazards) {
		final EditHazardsModel model = new EditHazardsModel(worldHazards);
		final EditHazardsDialog dialog = new EditHazardsDialog(model, rsrc);
		
		// Blocks due to dialog modality
		dialog.setLocationRelativeTo(null);
		dialog.setModal(true);
		dialog.setVisible(true);

		return dialog.model;
	}
	
}
