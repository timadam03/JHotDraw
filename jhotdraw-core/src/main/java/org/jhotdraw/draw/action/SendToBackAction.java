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
import org.jhotdraw.util.ReversedList;

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

    public static void sendToBack(DrawingView view, Collection<Figure> figures) {
        Drawing drawing = view.getDrawing();
        // sort is back-to-front; reverse so the back-most selected stays behind the others
        for (Figure figure : new ReversedList<>(drawing.sort(figures))) {
            drawing.sendToBack(figure);
        }
    }
}
