/*
 * @(#)BringForwardAction.java
 *
 * Copyright (c) 2003-2008 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.draw.action;

import org.jhotdraw.draw.figure.Figure;
import java.util.*;
import org.jhotdraw.draw.*;
import org.jhotdraw.util.ResourceBundleUtil;

/**
 * BringForwardAction.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class BringForwardAction extends ZOrderAction {

    private static final long serialVersionUID = 1L;
    public static final String ID = "edit.bringForward";

    /**
     * Creates a new instance.
     */
    public BringForwardAction(DrawingEditor editor) {
        super(editor, ID);
        ResourceBundleUtil labels
                = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
        labels.configureAction(this, ID);
        updateEnabledState();
    }

    @Override
    protected void moveFigures(DrawingView view, Collection<Figure> figures) {
        bringForward(view, figures);
    }

    public static void bringForward(DrawingView view, Collection<Figure> figures) {
        Drawing drawing = view.getDrawing();
        ArrayList<Figure> ordered = new ArrayList<>(figures);
        // Higher indices first so a multi-selection does not climb over itself.
        ordered.sort(Comparator.comparingInt(drawing::indexOf).reversed());
        for (Figure figure : ordered) {
            int index = drawing.indexOf(figure);
            if (index < drawing.getChildCount() - 1) {
                drawing.basicRemove(figure);
                drawing.basicAdd(index + 1, figure);
            }
        }
    }
}
