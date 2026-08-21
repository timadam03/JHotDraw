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
        final LinkedList<Figure> figures = new LinkedList<>(view.getSelectedFigures());
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
                undoMoveFigures(view, figures);
            }
        });
    }

    protected abstract void moveFigures(DrawingView view, Collection<Figure> figures);

    protected abstract void undoMoveFigures(DrawingView view, Collection<Figure> figures);
}
