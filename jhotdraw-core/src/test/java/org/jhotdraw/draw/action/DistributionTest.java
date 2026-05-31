/*
 * DistributionTest.java
 *
 * New file added for the align and distribute feature.
 * JHotDraw is distributed under the GNU LGPL v2.1.
 */
package org.jhotdraw.draw.action;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

/**
 * Unit tests for the {@link Distribution} geometry.
 *
 * <p>Distribution is the new feature, so it gets its own best case and boundary
 * tests. The method under test maps a list of boxes to a list of new
 * coordinates, so the tests check the returned array directly.</p>
 *
 * <p>Java note to self. {@code assertArrayEquals(expected, actual, EPS)} checks
 * two double arrays element by element within a tolerance. The best case fixes
 * three boxes with an uneven gap and checks the middle one moves to even the
 * spacing. The boundary cases are the two inputs where distribution must do
 * nothing, a selection of two boxes and of one box, because there is no inner
 * figure to move.</p>
 */
public class DistributionTest {

    private static final double EPS = 1e-9;

    private Rectangle2D.Double box(double x, double y, double w, double h) {
        return new Rectangle2D.Double(x, y, w, h);
    }

    // ---- Best case ----

    @Test
    public void horizontalEqualisesGapsBetweenThreeBoxes() {
        // Three 10 wide boxes. Left at x=0, right at x=100, middle bunched at 20.
        // Total width = 30, span = 110, leftover = 80 over 2 gaps = 40 each.
        // Positions become 0, then 0+10+40=50, then the end stays at 100.
        List<Rectangle2D.Double> boxes = Arrays.asList(
                box(0, 0, 10, 10),
                box(20, 0, 10, 10),
                box(100, 0, 10, 10));
        double[] xs = Distribution.HORIZONTAL.newPositions(boxes);
        assertArrayEquals(new double[]{0, 50, 100}, xs, EPS);
    }

    @Test
    public void verticalEqualisesGapsBetweenThreeBoxes() {
        // Same idea on the y axis.
        List<Rectangle2D.Double> boxes = Arrays.asList(
                box(0, 0, 10, 10),
                box(0, 20, 10, 10),
                box(0, 100, 10, 10));
        double[] ys = Distribution.VERTICAL.newPositions(boxes);
        assertArrayEquals(new double[]{0, 50, 100}, ys, EPS);
    }

    @Test
    public void distributionKeepsTheOutermostBoxesFixed() {
        List<Rectangle2D.Double> boxes = Arrays.asList(
                box(0, 0, 10, 10),
                box(33, 0, 10, 10),
                box(70, 0, 10, 10),
                box(100, 0, 10, 10));
        double[] xs = Distribution.HORIZONTAL.newPositions(boxes);
        // first and last never move
        assertEquals(0, xs[0], EPS);
        assertEquals(100, xs[3], EPS);
    }

    @Test
    public void inputOrderDoesNotChangeTheResultMapping() {
        // The same three boxes as the best case but supplied out of order. The
        // result lines up with the input by index, so the box that started at
        // x=20 (index 0 here) still ends up at the middle coordinate 50.
        List<Rectangle2D.Double> boxes = Arrays.asList(
                box(20, 0, 10, 10),
                box(100, 0, 10, 10),
                box(0, 0, 10, 10));
        double[] xs = Distribution.HORIZONTAL.newPositions(boxes);
        assertEquals(50, xs[0], EPS);
        assertEquals(100, xs[1], EPS);
        assertEquals(0, xs[2], EPS);
    }

    // ---- Boundary cases ----

    @Test
    public void twoBoxesAreLeftUnchanged() {
        List<Rectangle2D.Double> boxes = Arrays.asList(
                box(0, 0, 10, 10),
                box(100, 0, 10, 10));
        double[] xs = Distribution.HORIZONTAL.newPositions(boxes);
        assertArrayEquals(new double[]{0, 100}, xs, EPS);
    }

    @Test
    public void oneBoxIsLeftUnchanged() {
        List<Rectangle2D.Double> boxes = Arrays.asList(box(42, 0, 10, 10));
        double[] xs = Distribution.HORIZONTAL.newPositions(boxes);
        assertArrayEquals(new double[]{42}, xs, EPS);
    }
}
