/*
 * @(#)SendToBackAction.java
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
 * SendToBackAction.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class SendToBackAction extends ZOrderAction {

    private static final long serialVersionUID = 1L;
    public static final String ID = "edit.sendToBack";

    /**
     * Creates a new instance.
     */
    public SendToBackAction(DrawingEditor editor) {
        super(editor, ID);
        ResourceBundleUtil labels
                = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
        labels.configureAction(this, ID);
        updateEnabledState();
    }

    @Override
    protected void moveFigures(DrawingView view, Collection<Figure> figures) {
        sendToBack(view, figures);
    }

    @Override
    protected void undoMoveFigures(DrawingView view, Collection<Figure> figures) {
        BringToFrontAction.bringToFront(view, figures);
    }

    public static void sendToBack(DrawingView view, Collection<Figure> figures) {
        Drawing drawing = view.getDrawing();
        for (Figure figure : figures) { // XXX Shouldn't the figures be sorted here back to front?
            drawing.sendToBack(figure);
        }
    }
}
