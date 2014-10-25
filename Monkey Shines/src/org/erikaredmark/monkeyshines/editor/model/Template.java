package org.erikaredmark.monkeyshines.editor.model;

import java.util.HashSet;
import java.util.Set;

import org.erikaredmark.monkeyshines.Tile;
import org.erikaredmark.monkeyshines.tiles.CommonTile;
import org.erikaredmark.monkeyshines.tiles.TileType;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

/**
 * 
 * Models the idea of a 'set' of tiles of any type, of any id, in some standard configuration. Templates can be created, loaded, and
 * saved by the editor and assist in designing levels that, for design purposes, tiles designed to go with each other to form one
 * bigger object (such as the tree leaves in 'In The Swing', or cobwebs in 'Spooked'
 * <p/>
 * Instances of this class are immutable. When building or adding to a template, the builder class is used to create a new template.
 * 
 * @author Erika Redmark
 *
 */
public final class Template {
	
	/**
	 * 
	 * Draws the given template to the level tiles. The passed array should be the same array
	 * backing the LevelScreen that we wish to draw the template onto, hence it is a 20x32 array.
	 * <p/>
	 * Drawing by default starts at row, col as the <strong>top-left</strong> of the template. However,
	 * the row/col offsets can be passed to change this, hence making the location be, for example, the
	 * center of a 3x3 template by passing a rowOffset of 1 and a colOffset of 1. Negatives values are allowed
	 * but probably wouldn't make much sense... the location clicked wouldn't be within the template range.
	 * <p/>
	 * The default state for drawing is that both empty and non-empty tiles that are to be replaced by this
	 * template, will be replaced. Any empty spaces in the 2D 'rectangle' that this template takes up
	 * are NOT part of the template. Only tiles specifically indicated as 'NO TILE' in the template will
	 * replace their position with emptiness.
	 * 
	 * @param levelTiles
	 * 		the backing array for the level tiles in the level screen being edited
	 * 
	 * @param row
	 * 		the row of the 'start' of drawing
	 * 
	 * @param col
	 * 		the col of the 'start' of drawing
	 * 
	 * @param rowOffset
	 * 		row offset from the top-left of the template that this should start drawing (see docs above for example)
	 * 
	 * @param colOffset
	 * 		col offset from the top-left of the template that this should start drawing (see docs above for example)
	 * 
	 */
	public void drawTo(final Tile[][] levelTiles, int row, int col, int rowOffset, int colOffset) {
		// TODO method stub
	}
	
	private Template(final ImmutableList<TemplateTile> templateTiles) {
		this.templateTiles = templateTiles;
	}
	
	/**
	 * 
	 * Allows one to construct instances of templates. The builder allows the addition, one at a time, of tiles to the template. This fits
	 * in with the way a template would typically be constructed by a user (one at a time)
	 * <p/>
	 * Since the builder can be used with a GUI, it allows a callback to be assigned whenever the underlying template is modified.
	 * 
	 * @author Erika Redmark
	 *
	 */
	public final class Builder {

		public Builder() { 
			callback = NO_FUNCTION;
		};
		
		public Builder(final Function<Set<TemplateTile>, Void> callback ) {
			this.callback = callback;
		}
		/**
		 * 
		 * Adds the given tile to the given position in this template. If a tile already exists at that position, it is bumped out and replaced.
		 * <p/>
		 * If this builder has a callback registered, that callback is called.
		 * 
		 * @param row
		 * 		row of tile, relative to the template design (not the world)
		 * 
		 * @param col
		 * 		column of tile, relative to the template design (not the world)
		 * 
		 * @param tile
		 * 		the tile type to create
		 * 
		 * @return
		 * 		this builder
		 * 
		 */
		public Builder addTile(int row, int col, TileType tile) {
			tiles.add(new TemplateTile(row, col, tile) );
			return this;
		}
		
		/**
		 * 
		 * Removes the tile at the given location. This is NOT the same as setting it to the empty tile (which means the template
		 * would replace whatever was there with 'empty'). It means nothing will be at that location.
		 * <p/>
		 * Does nothing if there isn't anything there to begin with.
		 * 
		 * @param row
		 * 		row of tile, relative to the template design (not the world)
		 * 
		 * @param col
		 * 		column of tile, relative to the template design (not the world)
		 * 
		 * @return
		 * 		this builder
		 * 
		 */
		public Builder removeTile(int row, int col) {
			// Tile type is irrelevant; equals and hashcode don't care.
			tiles.remove(new TemplateTile(row, col, CommonTile.NONE) );
			return this;
		}
		
		/**
		 * 
		 * Creates a new instance of the enclosing Template class. Calls to subsequent builds always generate new objects.
		 * 
		 * @return
		 * 		new instance of the template based on the builder
		 * 
		 */
		public Template build() {
			return new Template(ImmutableList.copyOf(tiles) );
		}
		
		// A set so that duplicates (tiles in the same position) are properly removed. Replaced with basic array list when converted
		// to a standard template since the only operation there is iteration over the list.
		private final Set<TemplateTile> tiles = new HashSet<TemplateTile>();
		private final Function<Set<TemplateTile>, Void> callback;
	}
	
	// Intended for inner builder class, but Java rules require it to be declared outside.
	private static final Function<Set<TemplateTile>, Void> NO_FUNCTION = new Function<Set<TemplateTile>, Void>() {
		@Override public Void apply(Set<TemplateTile> arg0) { return null; }
	};
	
	/**
	 * 
	 * Represents a single 'tile' of the templates. Templates are composed of many of these in different configurations. These
	 * represent a row/col in some imaginary space. 
	 * <p/>
	 * Tiles cannot overlap. Hence, equality of this object is defined by the row and column ONLY, NOT the tile type! This allows
	 * easy replacement
	 * 
	 * @author Erika Redmark
	 *
	 */
	private final class TemplateTile {
		
		public final int row;
		public final int col;
		
		public final TileType tile;
		
		public TemplateTile(int row, int col, final TileType tile) {
			this.row = row;
			this.col = col;
			this.tile = tile;
		}
		
		@Override public boolean equals(Object o) {
			if (this == o)  return true;
			if (!(o instanceof TemplateTile) )  return false;
			
			TemplateTile object = (TemplateTile) o;
			return    object.col == this.col
				   && object.row == this.row;
		}
		
		@Override public int hashCode() {
			int result = 17;
			result += result * 31 + row;
			result += result * 31 + col;
			return result;
		}
	}
	
	// Stores list of all tiles. We don't store them in a 2D array. We just need to iterate over them, examine their row/col, and
	// from that decide where to draw the tile in the real world. A 2D array would be wasteful
	private ImmutableList<TemplateTile> templateTiles;
}
