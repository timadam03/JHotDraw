/*
 * DistributeAction.java
 *
 * New file added for the align and distribute feature.
 * JHotDraw is distributed under the GNU LGPL v2.1.
 */
package org.jhotdraw.draw.action;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.figure.Figure;
import org.jhotdraw.draw.event.TransformEdit;
import org.jhotdraw.undo.CompositeEdit;
import org.jhotdraw.util.ResourceBundleUtil;

/**
 * Distributes the selected figures so the gaps between them are equal.
 *
 * <p>This is the new feature. The change request asked to align and distribute,
 * alignment already existed and was tidied up first, so this class adds the
 * distribute half on top of the clean structure.</p>
 *
 * <p>I wrote it to mirror {@link AlignAction} on purpose. It is an
 * {@link AbstractSelectedAction}, it wraps its work in a CompositeEdit so the
 * whole thing is one undo, and it hands the actual arithmetic to the
 * {@link Distribution} enum, exactly the way AlignAction hands its arithmetic to
 * Alignment. Reusing that shape is the payoff of the earlier refactoring. The new
 * feature drops into a tidy pattern instead of adding another copy of an old
 * loop.</p>
 *
 * <p>This also shows two of the SOLID principles in practice. The action handles
 * the editor and undo, the enum handles the geometry, so each has a single
 * responsibility. And adding distribution needed no change to AlignAction, which
 * is the open closed idea, the code is open to a new operation while the old one
 * stays closed to edits.</p>
 */
public abstract class DistributeAction extends AbstractSelectedAction {

    private static final long serialVersionUID = 1L;
    private final Distribution distribution;
    protected ResourceBundleUtil labels
            = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");

    public DistributeAction(DrawingEditor editor, Distribution distribution) {
        super(editor);
        this.distribution = distribution;
        updateEnabledState();
    }

    /**
     * Distribution needs at least three figures, two to pin the ends and one in
     * the middle to move, so the button stays disabled below that.
     */
    @Override
    public void updateEnabledState() {
        if (getView() != null) {
            setEnabled(getView().isEnabled()
                    && getView().getSelectionCount() > 2);
        } else {
            setEnabled(false);
        }
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        CompositeEdit edit = new CompositeEdit(labels.getString("edit.distribute.text"));
        fireUndoableEditHappened(edit);
        distributeFigures(new ArrayList<>(getView().getSelectedFigures()));
        fireUndoableEditHappened(edit);
    }

    /**
     * The template method shared by both directions.
     *
     * <p>It asks the {@link Distribution} strategy for the new leading coordinate
     * of every figure, then shifts each transformable figure by the difference
     * between where it is and where it should be. Only the one axis moves, the
     * other shift stays zero, so the figures keep their position on the other
     * axis. A shift of zero means the figure is already in place and is skipped.</p>
     */
    protected void distributeFigures(List<Figure> figures) {
        List<Rectangle2D.Double> boxes = new ArrayList<>();
        for (Figure f : figures) {
            boxes.add(f.getBounds());
        }
        double[] targets = distribution.newPositions(boxes);
        for (int i = 0; i < figures.size(); i++) {
            Figure f = figures.get(i);
            if (!f.isTransformable()) {
                continue;
            }
            double current = distribution.position(boxes.get(i));
            double shift = targets[i] - current;
            if (shift == 0) {
                continue;
            }
            f.willChange();
            AffineTransform tx = new AffineTransform();
            if (distribution == Distribution.HORIZONTAL) {
                tx.translate(shift, 0);
            } else {
                tx.translate(0, shift);
            }
            f.transform(tx);
            f.changed();
            fireUndoableEditHappened(new TransformEdit(f, tx));
        }
    }

    public static class Horizontal extends DistributeAction {

        private static final long serialVersionUID = 1L;

        public Horizontal(DrawingEditor editor) {
            super(editor, Distribution.HORIZONTAL);
            labels.configureAction(this, "edit.distributeHorizontal");
        }

        public Horizontal(DrawingEditor editor, ResourceBundleUtil labels) {
            super(editor, Distribution.HORIZONTAL);
            labels.configureAction(this, "edit.distributeHorizontal");
        }
    }

    public static class Vertical extends DistributeAction {

        private static final long serialVersionUID = 1L;

        public Vertical(DrawingEditor editor) {
            super(editor, Distribution.VERTICAL);
            labels.configureAction(this, "edit.distributeVertical");
        }

        public Vertical(DrawingEditor editor, ResourceBundleUtil labels) {
            super(editor, Distribution.VERTICAL);
            labels.configureAction(this, "edit.distributeVertical");
        }
    }
}
