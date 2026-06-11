/*
 * Alignment.java
 *
 * New file added for the align and distribute feature.
 * JHotDraw is distributed under the GNU LGPL v2.1.
 */
package org.jhotdraw.draw.action;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * The six ways the editor can line up a group of selected figures.
 *
 * <p>I pulled this out of AlignAction while refactoring. Before, the six
 * directions each lived in their own subclass, and every subclass repeated the
 * same loop over the figures. The only thing that really differed between them
 * was one line of arithmetic, the amount each figure has to move. That
 * repetition is the Duplicated Code smell from Kerievsky chapter 4. Moving the
 * arithmetic here is the Extract Class step that removes it.</p>
 *
 * <p>Java note to self. An enum is normally just a fixed list of named values,
 * but in Java each value is allowed to carry its own behaviour by overriding a
 * method. That is what the constants below do, each one gives its own version of
 * delta(). So this single enum acts like a small family of six strategies
 * without needing six separate classes.</p>
 *
 * <p>Everything in here is pure geometry. It only reads rectangles and returns a
 * movement vector. There is no Swing and no drawing model, which is exactly what
 * makes it easy to unit test on its own.</p>
 *
 * <p>Coordinate reminder. A Rectangle2D.Double stores x and y as its top left
 * corner, plus width and height. Larger y means further down the screen. So the
 * top edge of a figure is figure.y and its bottom edge is figure.y + height.</p>
 */
public enum Alignment {

    /** Line the figures up on the top edge of the selection. */
    NORTH {
        @Override
        public Point2D.Double delta(Rectangle2D.Double figure, Rectangle2D.Double selection) {
            // Move vertically only. dx is 0. dy lifts the figure's top (figure.y)
            // up to the selection's top (selection.y).
            return new Point2D.Double(0, selection.y - figure.y);
        }
    },
    /** Line the figures up on the bottom edge of the selection. */
    SOUTH {
        @Override
        public Point2D.Double delta(Rectangle2D.Double figure, Rectangle2D.Double selection) {
            double targetBottom = selection.y + selection.height;
            // The figure's own bottom is figure.y + figure.height. dy is the gap
            // between that and the target bottom.
            return new Point2D.Double(0, targetBottom - figure.y - figure.height);
        }
    },
    /** Line the figures up on the left edge of the selection. */
    WEST {
        @Override
        public Point2D.Double delta(Rectangle2D.Double figure, Rectangle2D.Double selection) {
            // Move horizontally only, left edge to left edge.
            return new Point2D.Double(selection.x - figure.x, 0);
        }
    },
    /** Line the figures up on the right edge of the selection. */
    EAST {
        @Override
        public Point2D.Double delta(Rectangle2D.Double figure, Rectangle2D.Double selection) {
            double targetRight = selection.x + selection.width;
            return new Point2D.Double(targetRight - figure.x - figure.width, 0);
        }
    },
    /** Centre the figures on the horizontal middle line of the selection. */
    VERTICAL {
        @Override
        public Point2D.Double delta(Rectangle2D.Double figure, Rectangle2D.Double selection) {
            double targetCentreY = selection.y + selection.height / 2;
            // Compare the figure's own centre, not its edge, to the target.
            return new Point2D.Double(0, targetCentreY - figure.y - figure.height / 2);
        }
    },
    /** Centre the figures on the vertical middle line of the selection. */
    HORIZONTAL {
        @Override
        public Point2D.Double delta(Rectangle2D.Double figure, Rectangle2D.Double selection) {
            double targetCentreX = selection.x + selection.width / 2;
            return new Point2D.Double(targetCentreX - figure.x - figure.width / 2, 0);
        }
    };

    /**
     * Works out how far a figure has to move to line up with the selection.
     *
     * <p>The result is a vector, given as a Point2D.Double where x is the
     * sideways move and y is the up or down move. A zero vector means the figure
     * is already in place, so the caller can skip it. Returning a value instead
     * of moving the figure directly keeps this method free of side effects,
     * which is what lets a test call it and simply check the number.</p>
     *
     * <p>This is declared abstract, which means the enum itself does not provide
     * a body. Each constant above is forced to supply its own. That is the trick
     * that lets one method name behave six different ways.</p>
     *
     * @param figure    the bounding box of the figure being moved
     * @param selection the bounding box of the whole selection
     * @return the (dx, dy) movement, never null
     */
    public abstract Point2D.Double delta(Rectangle2D.Double figure, Rectangle2D.Double selection);
}
