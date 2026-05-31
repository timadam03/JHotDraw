/*
 * AlignmentTest.java
 *
 * New file added for the align and distribute feature.
 * JHotDraw is distributed under the GNU LGPL v2.1.
 */
package org.jhotdraw.draw.action;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the {@link Alignment} geometry.
 *
 * <p>These test the pure part of the align feature, the movement each direction
 * works out. This is exactly what the refactoring made possible. Before, the
 * same arithmetic was buried inside Swing action subclasses and could only be
 * checked by clicking the real buttons. Now it is a plain method from two
 * rectangles to a vector, so a test just sets up the input, calls the method,
 * and checks the number, with no editor and no mocks.</p>
 *
 * <p>Java notes to self. The {@code @Test} annotation marks a method JUnit runs.
 * {@code assertEquals(expected, actual, EPS)} compares two doubles and the third
 * argument is the tolerance, because comparing decimals for exact equality is
 * unreliable. The fixture below is two boxes, a fixed selection and a smaller
 * figure that moves, and each test comment states the expected delta.</p>
 */
public class AlignmentTest {

    private static final double EPS = 1e-9;

    /** Selection bounds: x=0, y=0, width=100, height=100. */
    private Rectangle2D.Double selection() {
        return new Rectangle2D.Double(0, 0, 100, 100);
    }

    /** A 20 by 20 figure sitting near the middle, at (40, 40). */
    private Rectangle2D.Double figure() {
        return new Rectangle2D.Double(40, 40, 20, 20);
    }

    // ---- Best case, each direction moves the figure where it should ----

    @Test
    public void northMovesTopEdgeToSelectionTop() {
        // top of selection is y=0, figure top is y=40, so dy = -40 and dx = 0
        Point2D.Double d = Alignment.NORTH.delta(figure(), selection());
        assertEquals(0, d.x, EPS);
        assertEquals(-40, d.y, EPS);
    }

    @Test
    public void southMovesBottomEdgeToSelectionBottom() {
        // selection bottom is y=100, figure bottom is 40+20=60, so dy = +40
        Point2D.Double d = Alignment.SOUTH.delta(figure(), selection());
        assertEquals(0, d.x, EPS);
        assertEquals(40, d.y, EPS);
    }

    @Test
    public void westMovesLeftEdgeToSelectionLeft() {
        // selection left is x=0, figure left is x=40, so dx = -40
        Point2D.Double d = Alignment.WEST.delta(figure(), selection());
        assertEquals(-40, d.x, EPS);
        assertEquals(0, d.y, EPS);
    }

    @Test
    public void eastMovesRightEdgeToSelectionRight() {
        // selection right is x=100, figure right is 40+20=60, so dx = +40
        Point2D.Double d = Alignment.EAST.delta(figure(), selection());
        assertEquals(40, d.x, EPS);
        assertEquals(0, d.y, EPS);
    }

    @Test
    public void verticalCentresOnHorizontalMidline() {
        // selection mid y = 50, figure centre y = 40 + 10 = 50, already centred
        Point2D.Double d = Alignment.VERTICAL.delta(figure(), selection());
        assertEquals(0, d.x, EPS);
        assertEquals(0, d.y, EPS);
    }

    @Test
    public void horizontalCentresOnVerticalMidline() {
        // selection mid x = 50, figure centre x = 50, already centred
        Point2D.Double d = Alignment.HORIZONTAL.delta(figure(), selection());
        assertEquals(0, d.x, EPS);
        assertEquals(0, d.y, EPS);
    }

    // ---- Boundary cases ----

    @Test
    public void alreadyAlignedFigureDoesNotMove() {
        // A figure already flush with the top should get a zero delta for NORTH.
        Rectangle2D.Double flushTop = new Rectangle2D.Double(10, 0, 20, 20);
        Point2D.Double d = Alignment.NORTH.delta(flushTop, selection());
        assertEquals(0, d.x, EPS);
        assertEquals(0, d.y, EPS);
    }

    @Test
    public void figureLargerThanSelectionStillProducesConsistentDelta() {
        // A figure wider than the selection still lines its left edge up for WEST.
        Rectangle2D.Double wide = new Rectangle2D.Double(5, 40, 200, 20);
        Point2D.Double d = Alignment.WEST.delta(wide, selection());
        assertEquals(-5, d.x, EPS);
        assertEquals(0, d.y, EPS);
    }

    @Test
    public void deltaIsNeverNull() {
        for (Alignment a : Alignment.values()) {
            assertEquals(false, a.delta(figure(), selection()) == null);
        }
    }
}
