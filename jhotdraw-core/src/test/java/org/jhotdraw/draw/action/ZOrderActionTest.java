/*
 * ZOrderActionTest.java
 *
 * Unit tests for stacking order: Bring to Front / Send to Back and one-layer
 * Forward / Backward. Real DefaultDrawing + RectangleFigure; mocked DrawingView.
 */
package org.jhotdraw.draw.action;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.swing.undo.UndoManager;
import org.jhotdraw.draw.DefaultDrawing;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.figure.Figure;
import org.jhotdraw.draw.figure.RectangleFigure;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the static move helpers and undo on {@link ZOrderAction}.
 * Child list: index 0 = back, last = front.
 */
public class ZOrderActionTest {

    private DrawingEditor editor;
    private Drawing drawing;
    private DrawingView view;
    private RectangleFigure a;
    private RectangleFigure b;
    private RectangleFigure c;
    private RectangleFigure d;

    @Before
    public void setUp() {
        editor = mock(DrawingEditor.class);
        view = mock(DrawingView.class);

        drawing = new DefaultDrawing();
        a = new RectangleFigure(0, 0, 10, 10);
        b = new RectangleFigure(20, 0, 10, 10);
        c = new RectangleFigure(40, 0, 10, 10);
        d = new RectangleFigure(60, 0, 10, 10);
        drawing.add(a);
        drawing.add(b);
        drawing.add(c);
        drawing.add(d);

        when(editor.getActiveView()).thenReturn(view);
        when(view.getDrawing()).thenReturn(drawing);
        when(view.isEnabled()).thenReturn(true);
        select();
    }

    // --- best case ---

    @Test
    public void bringToFrontMovesFigureToLastIndex() {
        BringToFrontAction.bringToFront(view, Collections.singleton(b));

        assertOrder(a, c, d, b);
        assertEquals("front is last in children", drawing.getChildCount() - 1, drawing.indexOf(b));
    }

    @Test
    public void sendToBackMovesFigureToIndexZero() {
        SendToBackAction.sendToBack(view, Collections.singleton(c));

        assertOrder(c, a, b, d);
        assertEquals("back is index 0", 0, drawing.indexOf(c));
    }

    @Test
    public void bringForwardMovesFigureOneLayer() {
        BringForwardAction.bringForward(view, Collections.singleton(b));

        assertOrder(a, c, b, d);
        assertEquals(2, drawing.indexOf(b));
    }

    @Test
    public void sendBackwardMovesFigureOneLayer() {
        SendBackwardAction.sendBackward(view, Collections.singleton(c));

        assertOrder(a, c, b, d);
        assertEquals(1, drawing.indexOf(c));
    }

    @Test
    public void multiSelectKeepsRelativeOrder() {
        BringToFrontAction.bringToFront(view, Arrays.asList(b, c));
        assertOrder(a, d, b, c);

        resetStack();
        SendToBackAction.sendToBack(view, Arrays.asList(b, c));
        assertOrder(b, c, a, d);

        resetStack();
        BringForwardAction.bringForward(view, Arrays.asList(b, c));
        assertOrder(a, d, b, c);

        resetStack();
        SendBackwardAction.sendBackward(view, Arrays.asList(b, c));
        assertOrder(b, c, a, d);
    }

    // --- boundary ---

    @Test
    public void stackingWithOneFigureIsStable() {
        drawing = new DefaultDrawing();
        RectangleFigure only = new RectangleFigure(0, 0, 10, 10);
        drawing.add(only);
        when(view.getDrawing()).thenReturn(drawing);

        BringToFrontAction.bringToFront(view, Collections.singleton(only));
        assertEquals(0, drawing.indexOf(only));
        SendToBackAction.sendToBack(view, Collections.singleton(only));
        assertEquals(0, drawing.indexOf(only));
        BringForwardAction.bringForward(view, Collections.singleton(only));
        assertEquals(0, drawing.indexOf(only));
        SendBackwardAction.sendBackward(view, Collections.singleton(only));
        assertEquals(0, drawing.indexOf(only));
        assertEquals(1, drawing.getChildCount());
    }

    @Test
    public void bringForwardAtFrontIsNoOp() {
        BringForwardAction.bringForward(view, Collections.singleton(d));

        assertOrder(a, b, c, d);
        assertEquals(drawing.getChildCount() - 1, drawing.indexOf(d));
    }

    @Test
    public void sendBackwardAtBackIsNoOp() {
        SendBackwardAction.sendBackward(view, Collections.singleton(a));

        assertOrder(a, b, c, d);
        assertEquals(0, drawing.indexOf(a));
    }

    @Test
    public void emptySelectionDoesNotCrash() {
        Set<Figure> empty = Collections.emptySet();
        BringToFrontAction.bringToFront(view, empty);
        SendToBackAction.sendToBack(view, empty);
        BringForwardAction.bringForward(view, empty);
        SendBackwardAction.sendBackward(view, empty);
        assertOrder(a, b, c, d);

        BringToFrontAction action = new BringToFrontAction(editor);
        action.actionPerformed(new ActionEvent(action, ActionEvent.ACTION_PERFORMED, BringToFrontAction.ID));
        assertOrder(a, b, c, d);
    }

    // --- invariant ---

    @Test
    public void unselectedFiguresDoNotMove() {
        BringForwardAction.bringForward(view, Collections.singleton(b));
        assertEquals("figure behind the selection stays put", 0, drawing.indexOf(a));
        assertEquals("figure two layers ahead stays put", 3, drawing.indexOf(d));
        assertOrder(a, c, b, d);

        resetStack();
        BringToFrontAction.bringToFront(view, Collections.singleton(b));
        assertEquals(0, drawing.indexOf(a));
        assertEquals(1, drawing.indexOf(c));
        assertEquals(2, drawing.indexOf(d));
        assertEquals("unselected keep relative order behind the selected figure",
                3, drawing.indexOf(b));
        assertOrder(a, c, d, b);
    }

    @Test
    public void undoRestoresIndices() {
        select(b);
        int indexA = drawing.indexOf(a);
        int indexB = drawing.indexOf(b);
        int indexC = drawing.indexOf(c);
        int indexD = drawing.indexOf(d);

        UndoManager mgr = new UndoManager();
        drawing.addUndoableEditListener(mgr);

        BringToFrontAction action = new BringToFrontAction(editor);
        action.actionPerformed(new ActionEvent(action, ActionEvent.ACTION_PERFORMED, BringToFrontAction.ID));
        assertEquals(3, drawing.indexOf(b));

        mgr.undo();
        assertEquals(indexA, drawing.indexOf(a));
        assertEquals(indexB, drawing.indexOf(b));
        assertEquals(indexC, drawing.indexOf(c));
        assertEquals(indexD, drawing.indexOf(d));
        assertOrder(a, b, c, d);
    }

    private void select(Figure... figures) {
        Set<Figure> selected = new LinkedHashSet<>();
        Collections.addAll(selected, figures);
        when(view.getSelectedFigures()).thenReturn(selected);
        when(view.getSelectionCount()).thenReturn(selected.size());
    }

    private void resetStack() {
        drawing.basicRemove(a);
        drawing.basicRemove(b);
        drawing.basicRemove(c);
        drawing.basicRemove(d);
        drawing.basicAdd(a);
        drawing.basicAdd(b);
        drawing.basicAdd(c);
        drawing.basicAdd(d);
    }

    private void assertOrder(Figure... expected) {
        assertEquals(Arrays.asList(expected), drawing.getChildren());
    }
}
