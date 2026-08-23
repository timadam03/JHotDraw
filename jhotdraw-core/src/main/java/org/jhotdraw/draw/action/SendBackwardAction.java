/*
 * @(#)SendBackwardAction.java
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
 * SendBackwardAction.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class SendBackwardAction extends ZOrderAction {

    private static final long serialVersionUID = 1L;
    public static final String ID = "edit.sendBackward";

    /**
     * Creates a new instance.
     */
    public SendBackwardAction(DrawingEditor editor) {
        super(editor, ID);
        ResourceBundleUtil labels
                = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
        labels.configureAction(this, ID);
        updateEnabledState();
    }

    @Override
    protected void moveFigures(DrawingView view, Collection<Figure> figures) {
        sendBackward(view, figures);
    }

    public static void sendBackward(DrawingView view, Collection<Figure> figures) {
        Drawing drawing = view.getDrawing();
        ArrayList<Figure> ordered = new ArrayList<>(figures);
        // Lower indices first so a multi-selection does not climb over itself.
        ordered.sort(Comparator.comparingInt(drawing::indexOf));
        for (Figure figure : ordered) {
            int index = drawing.indexOf(figure);
            if (index > 0) {
                drawing.basicRemove(figure);
                drawing.basicAdd(index - 1, figure);
            }
        }
    }
}
