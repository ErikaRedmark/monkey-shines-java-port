package org.erikaredmark.monkeyshines.background;

/**
 * 
 * Backgrounds are always the first thing drawn on a level. Different background types draw
 * backgrounds in different ways.
 * <p/>
 * Background objects hold arbitrary information for drawing the background, but never
 * the actual graphical data. Dedicated renderers are responsible for checking the limited
 * subclasses available and drawing properly based on that data.
 * <p/>
 * All backgrounds should be immutable, as references may be shared in memory.
 * 
 * @author Erika Redmark
 *
 */
public abstract class Background { }
