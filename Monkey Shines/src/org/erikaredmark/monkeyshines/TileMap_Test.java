package org.erikaredmark.monkeyshines;

import static org.junit.Assert.*;

import org.erikaredmark.monkeyshines.TileMap.Direction;
import org.erikaredmark.monkeyshines.tiles.CollapsibleTile;
import org.erikaredmark.monkeyshines.tiles.CommonTile;
import org.erikaredmark.monkeyshines.tiles.CommonTile.StatelessTileType;
import org.erikaredmark.monkeyshines.tiles.TileType;
import org.junit.Test;

public final class TileMap_Test {

	// constants for resize tests
	public static TileType FIRST_SOLID = CommonTile.of(1, StatelessTileType.SOLID);
	public static TileType FIRST_THRU = CommonTile.of(4, StatelessTileType.THRU);
	public static TileType FIRST_SCENE = CommonTile.of(5, StatelessTileType.SCENE);
	public static TileType SECOND_SOLID = CommonTile.of(3, StatelessTileType.SOLID);
	// Creates a common map for all resize tests:
	// s = solid, t = thru, x = scene, number indicates id.
	// [ s1 ] [    ] [    ]
	// [    ] [ t4 ] [ x5 ]
	// [    ] [ s3 ] [    ]
	public static TileMap createResizeMap() {
		TileMap map = new TileMap(3, 3);
		
		map.setTileRowCol(0, 0, FIRST_SOLID);
		map.setTileRowCol(1, 1, FIRST_THRU);
		map.setTileRowCol(1, 2, FIRST_SCENE);
		map.setTileRowCol(2, 1, SECOND_SOLID);
		
		return map;
	}
	
	public static void testTwoMaps(TileType[] expected, TileType[] actual) {
		assertEquals(expected.length, actual.length);
		for (int i = 0; i < actual.length; ++i) {
			assertEquals(actual[i], expected[i]);
		}
	}
	// [    ] [ s1 ] [    ] [    ]
	// [    ] [    ] [ t4 ] [ x5 ]
	// [    ] [    ] [ s3 ] [    ]
	@Test public void resizeExpandLeft() {
		TileMap expanded = createResizeMap().resize(1, Direction.WEST);
		assertEquals(3, expanded.getRowCount() );
		assertEquals(4, expanded.getColumnCount() );
		TileType[] underlyingMap = expanded.internalMap();
		TileType[] expectedIterationOrder = new TileType[] {
			CommonTile.NONE, FIRST_SOLID, CommonTile.NONE, CommonTile.NONE,
			CommonTile.NONE, CommonTile.NONE, FIRST_THRU, FIRST_SCENE,
			CommonTile.NONE, CommonTile.NONE, SECOND_SOLID, CommonTile.NONE
		};
		
		testTwoMaps(expectedIterationOrder, underlyingMap);
	}
	
	// [    ] [    ]
	// [ t4 ] [ x5 ]
	// [ s3 ] [    ]
	@Test public void resizeContractLeft() {
		TileMap expanded = createResizeMap().resize(-1, Direction.WEST);
		assertEquals(3, expanded.getRowCount() );
		assertEquals(2, expanded.getColumnCount() );
		TileType[] underlyingMap = expanded.internalMap();
		TileType[] expectedIterationOrder = new TileType[] {
			CommonTile.NONE, CommonTile.NONE,
			FIRST_THRU, FIRST_SCENE,
			SECOND_SOLID, CommonTile.NONE
		};
		
		testTwoMaps(expectedIterationOrder, underlyingMap);
	}
	
	// [ s1 ] [    ] [    ] [    ]
	// [    ] [ t4 ] [ x5 ] [    ]
	// [    ] [ s3 ] [    ] [    ]
	@Test public void resizeExpandRight() {
		TileMap expanded = createResizeMap().resize(1, Direction.EAST);
		assertEquals(3, expanded.getRowCount() );
		assertEquals(4, expanded.getColumnCount() );
		TileType[] underlyingMap = expanded.internalMap();
		TileType[] expectedIterationOrder = new TileType[] {
			FIRST_SOLID, CommonTile.NONE, CommonTile.NONE, CommonTile.NONE,
			CommonTile.NONE, FIRST_THRU, FIRST_SCENE, CommonTile.NONE,
			CommonTile.NONE, SECOND_SOLID, CommonTile.NONE, CommonTile.NONE
		};
		
		testTwoMaps(expectedIterationOrder, underlyingMap);
	}
	
	// [ s1 ] [    ]
	// [    ] [ t4 ]
	// [    ] [ s3 ]
	@Test public void resizeContractRight() {
		TileMap expanded = createResizeMap().resize(-1, Direction.EAST);
		assertEquals(3, expanded.getRowCount() );
		assertEquals(2, expanded.getColumnCount() );
		TileType[] underlyingMap = expanded.internalMap();
		TileType[] expectedIterationOrder = new TileType[] {
			FIRST_SOLID, CommonTile.NONE,
			CommonTile.NONE, FIRST_THRU,
			CommonTile.NONE, SECOND_SOLID
		};
		
		testTwoMaps(expectedIterationOrder, underlyingMap);
	}
	
	// [    ] [    ] [    ]
	// [ s1 ] [    ] [    ]
	// [    ] [ t4 ] [ x5 ]
	// [    ] [ s3 ] [    ]
	@Test public void resizeExpandUp() {
		TileMap expanded = createResizeMap().resize(1, Direction.NORTH);
		assertEquals(4, expanded.getRowCount() );
		assertEquals(3, expanded.getColumnCount() );
		TileType[] underlyingMap = expanded.internalMap();
		TileType[] expectedIterationOrder = new TileType[] {
			CommonTile.NONE, CommonTile.NONE, CommonTile.NONE,
			FIRST_SOLID, CommonTile.NONE, CommonTile.NONE,
			CommonTile.NONE, FIRST_THRU, FIRST_SCENE,
			CommonTile.NONE, SECOND_SOLID, CommonTile.NONE
		};
		
		testTwoMaps(expectedIterationOrder, underlyingMap);
	}
	
	// [    ] [ t4 ] [ x5 ]
	// [    ] [ s3 ] [    ]
	@Test public void resizeContractUp() {
		TileMap expanded = createResizeMap().resize(-1, Direction.NORTH);
		assertEquals(2, expanded.getRowCount() );
		assertEquals(3, expanded.getColumnCount() );
		TileType[] underlyingMap = expanded.internalMap();
		TileType[] expectedIterationOrder = new TileType[] {
			CommonTile.NONE, FIRST_THRU, FIRST_SCENE,
			CommonTile.NONE, SECOND_SOLID, CommonTile.NONE
		};
		
		testTwoMaps(expectedIterationOrder, underlyingMap);
	}
	
	// [ s1 ] [    ] [    ]
	// [    ] [ t4 ] [ x5 ]
	// [    ] [ s3 ] [    ]
	// [    ] [    ] [    ]
	@Test public void resizeExpandDown() {
		TileMap expanded = createResizeMap().resize(1, Direction.SOUTH);
		assertEquals(4, expanded.getRowCount() );
		assertEquals(3, expanded.getColumnCount() );
		TileType[] underlyingMap = expanded.internalMap();
		TileType[] expectedIterationOrder = new TileType[] {
			FIRST_SOLID, CommonTile.NONE, CommonTile.NONE,
			CommonTile.NONE, FIRST_THRU, FIRST_SCENE,
			CommonTile.NONE, SECOND_SOLID, CommonTile.NONE,
			CommonTile.NONE, CommonTile.NONE, CommonTile.NONE
		};
		
		testTwoMaps(expectedIterationOrder, underlyingMap);
	}
	
	// [ s1 ] [    ] [    ]
	// [    ] [ t4 ] [ x5 ]
	@Test public void resizeContractDown() {
		TileMap expanded = createResizeMap().resize(-1, Direction.SOUTH);
		assertEquals(2, expanded.getRowCount() );
		assertEquals(3, expanded.getColumnCount() );
		TileType[] underlyingMap = expanded.internalMap();
		TileType[] expectedIterationOrder = new TileType[] {
			FIRST_SOLID, CommonTile.NONE, CommonTile.NONE,
			CommonTile.NONE, FIRST_THRU, FIRST_SCENE
		};
		
		testTwoMaps(expectedIterationOrder, underlyingMap);
	}
	
	// Equality Tests
	@Test public void equalMaps() {
		TileMap first = new TileMap(4, 3);
		
		first.setTileRowCol(0, 0, CommonTile.of(13, StatelessTileType.SOLID) );
		first.setTileRowCol(3, 1, CommonTile.of(3, StatelessTileType.THRU) );
		first.setTileRowCol(1, 2, new CollapsibleTile(1) );
		first.setTileRowCol(3, 2, CommonTile.of(4, StatelessTileType.SCENE) );
		
		TileMap second = new TileMap(4, 3);
		
		second.setTileRowCol(0, 0, CommonTile.of(13, StatelessTileType.SOLID) );
		second.setTileRowCol(3, 1, CommonTile.of(3, StatelessTileType.THRU) );
		second.setTileRowCol(1, 2, new CollapsibleTile(1) );
		second.setTileRowCol(3, 2, CommonTile.of(4, StatelessTileType.SCENE) );
		
		assertEquals(first, second);
		assertEquals(second, first);
		
		assertEquals(first.hashCode(), second.hashCode() );
	}
	
	@Test public void unequalMapsWrongTypes() {
		TileMap first = new TileMap(4, 3);
		
		first.setTileRowCol(0, 0, CommonTile.of(13, StatelessTileType.SOLID) );
		first.setTileRowCol(3, 1, CommonTile.of(3, StatelessTileType.THRU) );
		first.setTileRowCol(1, 2, new CollapsibleTile(1) );
		first.setTileRowCol(3, 2, CommonTile.of(4, StatelessTileType.SCENE) );
		
		TileMap second = new TileMap(4, 3);
		
		second.setTileRowCol(0, 0, CommonTile.of(13, StatelessTileType.SOLID) );
		second.setTileRowCol(3, 1, CommonTile.of(3, StatelessTileType.SOLID) );
		second.setTileRowCol(1, 2, new CollapsibleTile(1) );
		second.setTileRowCol(3, 2, CommonTile.of(4, StatelessTileType.SCENE) );
		
		assertNotEquals(first, second);
		assertNotEquals(second, first);
	}
	
	@Test public void unequalMapsWrongSize() {
		TileMap first = new TileMap(4, 4);
		
		first.setTileRowCol(0, 0, CommonTile.of(13, StatelessTileType.SOLID) );
		first.setTileRowCol(1, 1, CommonTile.of(3, StatelessTileType.THRU) );
		
		TileMap second = new TileMap(8, 8);
		
		second.setTileRowCol(0, 0, CommonTile.of(13, StatelessTileType.SOLID) );
		second.setTileRowCol(1, 1, CommonTile.of(3, StatelessTileType.THRU) );
		
		assertNotEquals(first, second);
		assertNotEquals(second, first);
	}
}
