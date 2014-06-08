package org.erikaredmark.monkeyshines.editor.dialog;

import java.nio.file.Path;

import org.erikaredmark.util.ObservableModel;

/**
 * 
 * Backing model class for new world dialog. Each created dialog creates a single backing model to store its state
 * in. This model also contains the operations that are done on the data, such as actually creating the world and
 * the proper level screen editor information. 
 * 
 * @author Erika Redmark
 *
 */
public final class NewWorldDialogModel extends ObservableModel {

	private boolean useDefaultPack;
	private Path selectedResourcePack;
	private String worldName;
	
	// This is directly modified by the dialog itself. It is only ever changed when
	// the dialog is closing. It fires no property event as clients activating the dialog
	// are expected to query its state.
	Path saveLocation;
	
	/** Fired when the boolean for 'using the default resource pack' is toggled. The event will be a {@code Boolean}
	 *  that is either {@code true} for using the default pack, or {@code false} otherwise								*/
	public static final String PROPERTY_DEFAULT_PACK = "propDefaultPack";
	
	/** Fired when then path to the resource pack is modified. The associated events will be of type {@code Path}
	 *  but the old may be {@code null} if this is the first time a path is being assigned.								*/
	public static final String PROPERTY_PACK = "propPack";
	
	/** Fired when the world name is changed. Both types are {@code String} and may be empty but never {@code null}     */
	public static final String PROPERTY_WORLD_NAME = "propName";
	
	private NewWorldDialogModel() {
		this.worldName = "";
		this.selectedResourcePack = null;
		this.useDefaultPack = true;
		this.saveLocation = null;
	}

	public static NewWorldDialogModel newInstance() {
		return new NewWorldDialogModel();
	}


	public boolean isUseDefaultPack() { return useDefaultPack; }
	public Path getSelectedResourcePack() { return selectedResourcePack; }
	public String getWorldName() { return worldName; }
	
	/**
	 * 
	 * Returns the location the new world is saved at. Until this dialog succeeds in making a world, this value
	 * is {@code null}. It will also be {@code null} on error
	 * 
	 * @return
	 * 		the location the new .world file was saved to. {@code null} if save did not complete
	 * 
	 */
	public Path getSaveLocation() { return saveLocation; }

	/** Sets whether the default texture pack is used and fires {@code PROPERTY_DEFAULT_PACK}							*/
	public void setUseDefaultPack(boolean useDefaultPack) {
		Boolean old = this.useDefaultPack;
		this.useDefaultPack = useDefaultPack;
		firePropertyChange(PROPERTY_DEFAULT_PACK, old, this.useDefaultPack);
	}

	/** Sets the current path for the selected resource pack and fires {@code PROPERTY_PACK}							*/
	public void setSelectedResourcePack(Path selectedResourcePack) {
		Path old = this.selectedResourcePack;
		this.selectedResourcePack = selectedResourcePack;
		firePropertyChange(PROPERTY_PACK, old, this.selectedResourcePack);
	}

	/** Sets the current world name and fires {@code PROPERTY_WORLD_NAME}												*/
	public void setWorldName(String worldName) {
		String old = this.worldName;
		this.worldName = worldName;
		firePropertyChange(PROPERTY_WORLD_NAME, old, this.worldName);
	}
	
}
