/*
 * @(#)AlignAction.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.draw.action;

import java.awt.geom.*;
import java.util.*;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.figure.Figure;
import org.jhotdraw.draw.event.TransformEdit;
import org.jhotdraw.undo.CompositeEdit;
import org.jhotdraw.util.ResourceBundleUtil;

/**
 * Aligns the selected figures to a shared edge or centre line.
 *
 * <p>This is the controller for the align feature. When the user presses an
 * align button, Swing calls {@link #actionPerformed}.</p>
 *
 * <p>What I changed while refactoring. Originally each of the six directions was
 * a subclass that overrode the alignment method with the same loop, and only the
 * line that computed the movement differed. I lifted that shared loop up into one
 * method here, {@link #alignFigures}. This is the Form Template Method
 * refactoring, the base class holds the skeleton of the algorithm and the part
 * that varies is delegated out, in this case to the {@link Alignment} enum. The
 * user sees no difference, which is the defining rule of a refactoring, so the
 * existing callers in ButtonFactory and AlignToolBar still work unchanged.</p>
 *
 * <p>Java notes to self.</p>
 * <ul>
 *   <li>"abstract class" means you cannot create an AlignAction on its own, only
 *       one of the concrete subclasses at the bottom such as North or East.</li>
 *   <li>Those subclasses are static nested classes, classes declared inside this
 *       one. Callers write {@code new AlignAction.North(editor)}.</li>
 *   <li>"extends AbstractSelectedAction" means we inherit all the plumbing that
 *       tracks the current selection, so here we only write the align logic.</li>
 * </ul>
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public abstract class AlignAction extends AbstractSelectedAction {

    private static final long serialVersionUID = 1L;
    // Which direction this action aligns to. "final" means it is set once in the
    // constructor and can never change afterwards, which is what we want here.
    private final Alignment alignment;
    // The label texts and icons, looked up by key from a resource file. This is
    // how JHotDraw supports several languages.
    protected ResourceBundleUtil labels
            = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");

    /**
     * Creates an align action for the given direction. The subclasses below call
     * this with their own Alignment value.
     */
    public AlignAction(DrawingEditor editor, Alignment alignment) {
        super(editor);
        this.alignment = alignment;
        updateEnabledState();
    }

    /**
     * Decides when the button is clickable. Aligning needs at least two figures,
     * because a single figure has nothing to line up against.
     */
    @Override
    public void updateEnabledState() {
        if (getView() != null) {
            setEnabled(getView().isEnabled()
                    && getView().getSelectionCount() > 1);
        } else {
            setEnabled(false);
        }
    }

    /**
     * Runs when the user clicks the button. The two fireUndoableEditHappened
     * calls wrap the work in a single CompositeEdit, so one press of undo reverts
     * the whole alignment rather than moving figures back one at a time.
     */
    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        CompositeEdit edit = new CompositeEdit(labels.getString("edit.align.text"));
        fireUndoableEditHappened(edit);
        alignFigures(getView().getSelectedFigures(), getSelectionBounds());
        fireUndoableEditHappened(edit);
    }

    /**
     * The template method, the loop every direction shares.
     *
     * <p>It walks the selected figures, asks the {@link Alignment} strategy how
     * far each one must move, and applies that move. The direction specific
     * decision is the single call to {@code alignment.delta(...)}, so no subclass
     * has to repeat the loop.</p>
     *
     * <p>Step by step for each figure. {@code isTransformable} skips locked
     * figures. {@code willChange} tells the figure a change is starting so it can
     * prepare to repaint. An AffineTransform is Java's way to describe a move, and
     * here we use only its translate part. {@code transform} applies it,
     * {@code changed} signals the change is done, and the TransformEdit records
     * the move so it can be undone.</p>
     */
    protected void alignFigures(Collection<Figure> selectedFigures, Rectangle2D.Double selectionBounds) {
        for (Figure f : getView().getSelectedFigures()) {
            if (f.isTransformable()) {
                f.willChange();
                Rectangle2D.Double b = f.getBounds();
                Point2D.Double d = alignment.delta(b, selectionBounds);
                AffineTransform tx = new AffineTransform();
                tx.translate(d.x, d.y);
                f.transform(tx);
                f.changed();
                fireUndoableEditHappened(new TransformEdit(f, tx));
            }
        }
    }

    /**
     * Returns the smallest rectangle that contains all the selected figures.
     * Every direction lines up against the edges of this combined box.
     */
    protected Rectangle2D.Double getSelectionBounds() {
        Rectangle2D.Double bounds = null;
        for (Figure f : getView().getSelectedFigures()) {
            if (bounds == null) {
                bounds = f.getBounds();
            } else {
                // add() grows the rectangle so it also covers this figure.
                bounds.add(f.getBounds());
            }
        }
        return bounds;
    }

    // The six concrete actions. Each one only has to remember which Alignment it
    // is and set up its label. All the real work lives in the base class above.

    public static class North extends AlignAction {

        private static final long serialVersionUID = 1L;

        public North(DrawingEditor editor) {
            super(editor, Alignment.NORTH);
            labels.configureAction(this, "edit.alignNorth");
        }

        public North(DrawingEditor editor, ResourceBundleUtil labels) {
            super(editor, Alignment.NORTH);
            labels.configureAction(this, "edit.alignNorth");
        }
    }

    public static class East extends AlignAction {

        private static final long serialVersionUID = 1L;

        public East(DrawingEditor editor) {
            super(editor, Alignment.EAST);
            labels.configureAction(this, "edit.alignEast");
        }

        public East(DrawingEditor editor, ResourceBundleUtil labels) {
            super(editor, Alignment.EAST);
            labels.configureAction(this, "edit.alignEast");
        }
    }

    public static class West extends AlignAction {

        private static final long serialVersionUID = 1L;

        public West(DrawingEditor editor) {
            super(editor, Alignment.WEST);
            labels.configureAction(this, "edit.alignWest");
        }

        public West(DrawingEditor editor, ResourceBundleUtil labels) {
            super(editor, Alignment.WEST);
            labels.configureAction(this, "edit.alignWest");
        }
    }

    public static class South extends AlignAction {

        private static final long serialVersionUID = 1L;

        public South(DrawingEditor editor) {
            super(editor, Alignment.SOUTH);
            labels.configureAction(this, "edit.alignSouth");
        }

        public South(DrawingEditor editor, ResourceBundleUtil labels) {
            super(editor, Alignment.SOUTH);
            labels.configureAction(this, "edit.alignSouth");
        }
    }

    public static class Vertical extends AlignAction {

        private static final long serialVersionUID = 1L;

        public Vertical(DrawingEditor editor) {
            super(editor, Alignment.VERTICAL);
            labels.configureAction(this, "edit.alignVertical");
        }

        public Vertical(DrawingEditor editor, ResourceBundleUtil labels) {
            super(editor, Alignment.VERTICAL);
            labels.configureAction(this, "edit.alignVertical");
        }
    }

    public static class Horizontal extends AlignAction {

        private static final long serialVersionUID = 1L;

        public Horizontal(DrawingEditor editor) {
            super(editor, Alignment.HORIZONTAL);
            labels.configureAction(this, "edit.alignHorizontal");
        }

        public Horizontal(DrawingEditor editor, ResourceBundleUtil labels) {
            super(editor, Alignment.HORIZONTAL);
            labels.configureAction(this, "edit.alignHorizontal");
        }
    }
}
