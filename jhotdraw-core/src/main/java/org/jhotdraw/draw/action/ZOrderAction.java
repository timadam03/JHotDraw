/*
 * @(#)ZOrderAction.java
 *
 * Copyright (c) 2003-2008 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.draw.action;

import org.jhotdraw.draw.figure.Figure;
import java.util.*;
import javax.swing.undo.*;
import org.jhotdraw.draw.*;
import org.jhotdraw.util.ResourceBundleUtil;

/**
 * Shared parent for actions that change the stacking order of selected figures.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
abstract class ZOrderAction extends AbstractSelectedAction {

    private static final long serialVersionUID = 1L;
    private final String id;

    ZOrderAction(DrawingEditor editor, String id) {
        super(editor);
        this.id = id;
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        final DrawingView view = getView();
        final Drawing drawing = view.getDrawing();
        final LinkedList<Figure> figures = new LinkedList<>(view.getSelectedFigures());
        final HashMap<Figure, Integer> oldIndices = new HashMap<>();
        for (Figure figure : figures) {
            oldIndices.put(figure, drawing.indexOf(figure));
        }
        moveFigures(view, figures);
        fireUndoableEditHappened(new AbstractUndoableEdit() {
            private static final long serialVersionUID = 1L;

            @Override
            public String getPresentationName() {
                ResourceBundleUtil labels
                        = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
                return labels.getTextProperty(id);
            }

            @Override
            public void redo() throws CannotRedoException {
                super.redo();
                moveFigures(view, figures);
            }

            @Override
            public void undo() throws CannotUndoException {
                super.undo();
                restoreOldOrder(drawing, oldIndices);
            }
        });
    }

    // Remove all, then add back low-index first so target indices stay in range.
    private static void restoreOldOrder(Drawing drawing, Map<Figure, Integer> oldIndices) {
        ArrayList<Figure> ordered = new ArrayList<>(oldIndices.keySet());
        ordered.sort(Comparator.comparingInt(oldIndices::get));
        for (Figure figure : ordered) {
            drawing.basicRemove(figure);
        }
        for (Figure figure : ordered) {
            drawing.basicAdd(oldIndices.get(figure), figure);
        }
    }

    protected abstract void moveFigures(DrawingView view, Collection<Figure> figures);
}
