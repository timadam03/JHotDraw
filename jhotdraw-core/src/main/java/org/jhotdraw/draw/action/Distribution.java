/*
 * Distribution.java
 *
 * New file added for the align and distribute feature.
 * JHotDraw is distributed under the GNU LGPL v2.1.
 */
package org.jhotdraw.draw.action;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The two axes along which the editor can space figures out evenly.
 *
 * <p>This is the new half of the feature. It sits next to {@link Alignment} on
 * purpose. Alignment moves every figure onto one shared line, while distribution
 * spreads the figures so the gaps between neighbours are equal. Both are pure
 * geometry over bounding boxes, so neither one touches Swing or the drawing
 * model and both stay easy to unit test.</p>
 *
 * <p>Java note to self. Just like Alignment, this is an enum where each constant
 * supplies its own behaviour. The two constants below differ only in which axis
 * they read, x and width for horizontal, y and height for vertical. Pulling
 * those two readings into {@code position} and {@code size} lets the real
 * algorithm in {@code newPositions} be written once for both directions.</p>
 *
 * <p>The maths runs over the whole selection at once, not one figure at a time,
 * because where a figure ends up depends on the order and size of the others.
 * The two outermost figures stay where they are and the ones in between are
 * spread so the spacing is uniform.</p>
 */
public enum Distribution {

    /** Spread figures left to right so the horizontal gaps are equal. */
    HORIZONTAL {
        @Override
        public double position(Rectangle2D.Double r) {
            return r.x;
        }

        @Override
        public double size(Rectangle2D.Double r) {
            return r.width;
        }
    },
    /** Spread figures top to bottom so the vertical gaps are equal. */
    VERTICAL {
        @Override
        public double position(Rectangle2D.Double r) {
            return r.y;
        }

        @Override
        public double size(Rectangle2D.Double r) {
            return r.height;
        }
    };

    /** The leading coordinate of the box on this axis, x or y. */
    public abstract double position(Rectangle2D.Double r);

    /** The extent of the box on this axis, width or height. */
    public abstract double size(Rectangle2D.Double r);

    /**
     * Works out a new leading coordinate for every box so the gaps between
     * neighbours come out equal.
     *
     * <p>The result is a plain array of coordinates in the same order as the
     * input list, so the caller can match each new value back to its figure by
     * index. Returning numbers rather than moving figures keeps this testable.</p>
     *
     * <p>Distribution only makes sense with three or more figures, two to pin the
     * ends and at least one in the middle to move. With two or fewer the method
     * returns the coordinates unchanged. The action enforces the same minimum
     * before it even offers the command, so this is just a safety net.</p>
     *
     * @param boxes the bounding boxes of the selected figures, in any order
     * @return the new leading coordinate per box, lined up with the input by index
     */
    public double[] newPositions(List<Rectangle2D.Double> boxes) {
        int n = boxes.size();
        double[] result = new double[n];
        for (int i = 0; i < n; i++) {
            result[i] = position(boxes.get(i));
        }
        if (n < 3) {
            return result;
        }

        // Sort a copy of the indices by leading coordinate, so we can walk the
        // figures from one end of the selection to the other. We sort indices
        // rather than the boxes themselves so the answer still maps back by index.
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            order.add(i);
        }
        order.sort(Comparator.comparingDouble(i -> position(boxes.get(i))));

        int firstIdx = order.get(0);
        int lastIdx = order.get(n - 1);
        double start = position(boxes.get(firstIdx));
        double end = position(boxes.get(lastIdx));

        // Add up the space the figures themselves take, so the space left over
        // can be shared out as n-1 equal gaps.
        double totalSize = 0;
        for (Rectangle2D.Double b : boxes) {
            totalSize += size(b);
        }
        double span = (end + size(boxes.get(lastIdx))) - start;
        double gap = (span - totalSize) / (n - 1);

        // Walk the figures in order, placing each one right after the previous
        // one plus a single uniform gap. The first and last keep their place.
        double cursor = start;
        for (int k = 0; k < n; k++) {
            int idx = order.get(k);
            result[idx] = cursor;
            cursor += size(boxes.get(idx)) + gap;
        }
        result[lastIdx] = end;
        return result;
    }
}
