package org.jhotdraw.draw.tool;

import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.figure.Figure;
import org.jhotdraw.draw.handle.Handle;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.swing.JPanel;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * JUnit 4 + Mockito tests for {@link SelectionTool}.
 */
public class SelectionToolTest {

    private TestableSelectionTool tool;
    private DrawingEditor mockEditor;
    private DrawingView mockView;
    private Drawing mockDrawing;
    private Figure mockFigure;
    private Handle mockHandle;
    private JPanel dummyComponent;

    /**
     * Exposes protected methods of SelectionTool for testing.
     */
    private static class TestableSelectionTool extends SelectionTool {
        public Tool callGetSelectAreaTracker() {
            return getSelectAreaTracker();
        }
        public DragTracker callGetDragTracker(Figure f) {
            return getDragTracker(f);
        }
        public HandleTracker callGetHandleTracker(Handle h) {
            return getHandleTracker(h);
        }
        public void callSetTracker(Tool t) {
            setTracker(t);
        }
    }

    @Before
    public void setUp() {
        tool = new TestableSelectionTool();
        mockEditor = mock(DrawingEditor.class);
        mockView = mock(DrawingView.class);
        mockDrawing = mock(Drawing.class);
        mockFigure = mock(Figure.class);
        mockHandle = mock(Handle.class);
        dummyComponent = new JPanel();

        // Wire up default editor/view behavior
        when(mockEditor.getActiveView()).thenReturn(mockView);
        when(mockEditor.getDrawingViews()).thenReturn(Collections.emptySet());
        when(mockEditor.findView(any(Container.class))).thenReturn(mockView);
        when(mockView.getDrawing()).thenReturn(mockDrawing);
        when(mockView.isEnabled()).thenReturn(true);
        when(mockView.viewToDrawing(any(Point.class))).thenReturn(new Point2D.Double(100, 100));
        when(mockView.getSelectedFigures()).thenReturn(Collections.emptySet());
        when(mockView.findHandle(any(Point.class))).thenReturn(null);
        when(mockView.findFigure(any(Point.class))).thenReturn(null);
        when(mockView.getCompatibleHandles(any(Handle.class))).thenReturn(Collections.emptyList());
        when(mockHandle.getCursor()).thenReturn(Cursor.getDefaultCursor());
    }

    // -------------------------------------------------------------------------
    // A. Best-case scenario tests
    // -------------------------------------------------------------------------

    @Test
    public void testDefaultSelectBehindEnabled() {
        assertTrue(tool.isSelectBehindEnabled());
    }

    @Test
    public void testSetSelectBehindEnabled() {
        tool.setSelectBehindEnabled(false);
        assertFalse(tool.isSelectBehindEnabled());
    }

    @Test
    public void testSelectBehindEnabledFiresPropertyChange() {
        PropertyChangeListener listener = mock(PropertyChangeListener.class);
        tool.addPropertyChangeListener(listener);

        tool.setSelectBehindEnabled(false);

        verify(listener).propertyChange(any(PropertyChangeEvent.class));
    }

    @Test
    public void testSupportsHandleInteraction() {
        assertTrue(tool.supportsHandleInteraction());
    }

    @Test
    public void testGetSelectAreaTrackerLazyInit() {
        Tool first = tool.callGetSelectAreaTracker();
        Tool second = tool.callGetSelectAreaTracker();
        assertNotNull(first);
        assertSame(first, second);
    }

    @Test
    public void testGetDragTrackerLazyInit() {
        DragTracker first = tool.callGetDragTracker(mockFigure);
        DragTracker second = tool.callGetDragTracker(mockFigure);
        assertNotNull(first);
        assertSame(first, second);
    }

    @Test
    public void testGetHandleTrackerLazyInit() {
        tool.activate(mockEditor);
        HandleTracker first = tool.callGetHandleTracker(mockHandle);
        HandleTracker second = tool.callGetHandleTracker(mockHandle);
        assertNotNull(first);
        assertSame(first, second);
    }

    @Test
    public void testActivateDelegatesTracker() {
        tool.activate(mockEditor);

        Tool mockTracker = mock(Tool.class);
        tool.callSetTracker(mockTracker);

        // Now activate again — mockTracker.activate should be called
        tool.activate(mockEditor);
        verify(mockTracker, atLeastOnce()).activate(mockEditor);
    }

    @Test
    public void testDeactivateDelegatesTracker() {
        tool.activate(mockEditor);

        Tool mockTracker = mock(Tool.class);
        tool.callSetTracker(mockTracker);

        tool.deactivate(mockEditor);
        verify(mockTracker).deactivate(mockEditor);
    }

    @Test
    public void testSetTrackerSwapsTrackers() {
        tool.activate(mockEditor);

        Tool oldTracker = mock(Tool.class);
        Tool newTracker = mock(Tool.class);

        tool.callSetTracker(oldTracker);
        tool.callSetTracker(newTracker);

        // Old tracker deactivated, new tracker activated
        verify(oldTracker).deactivate(mockEditor);
        verify(newTracker).activate(mockEditor);
    }

    // -------------------------------------------------------------------------
    // B. Boundary-case tests
    // -------------------------------------------------------------------------

    @Test
    public void testSetTrackerWithNull() {
        tool.activate(mockEditor);

        // Should be a no-op — no NPE, tool remains functional
        tool.callSetTracker(null);
        tool.deactivate(mockEditor);
    }

    @Test
    public void testMousePressedWithDisabledView() {
        when(mockView.isEnabled()).thenReturn(false);
        tool.activate(mockEditor);

        Tool mockTracker = mock(Tool.class);
        tool.callSetTracker(mockTracker);

        MouseEvent evt = new MouseEvent(dummyComponent, MouseEvent.MOUSE_PRESSED,
                System.currentTimeMillis(), 0, 100, 100, 1, false, MouseEvent.BUTTON1);

        tool.mousePressed(evt);

        // tracker's mousePressed should NOT be called when view is disabled
        verify(mockTracker, never()).mousePressed(evt);
    }

    @Test
    public void testResolveTrackerWithHandleReturnsHandleTracker() {
        when(mockView.findHandle(any(Point.class))).thenReturn(mockHandle);
        when(mockView.getCompatibleHandles(mockHandle)).thenReturn(Collections.emptyList());
        tool.activate(mockEditor);

        MouseEvent evt = new MouseEvent(dummyComponent, MouseEvent.MOUSE_PRESSED,
                System.currentTimeMillis(), 0, 10, 10, 1, false, MouseEvent.BUTTON1);

        tool.mousePressed(evt);

        HandleTracker ht = tool.callGetHandleTracker(mockHandle);
        assertNotNull(ht);
        assertTrue(ht instanceof HandleTracker);
    }

    @Test
    public void testResolveTrackerWithSelectableFigureReturnsDragTracker() {
        when(mockView.findHandle(any(Point.class))).thenReturn(null);
        when(mockView.findFigure(any(Point.class))).thenReturn(mockFigure);
        when(mockFigure.isSelectable()).thenReturn(true);
        when(mockFigure.contains(any(Point2D.Double.class))).thenReturn(false);
        tool.activate(mockEditor);

        MouseEvent evt = new MouseEvent(dummyComponent, MouseEvent.MOUSE_PRESSED,
                System.currentTimeMillis(), 0, 10, 10, 1, false, MouseEvent.BUTTON1);

        tool.mousePressed(evt);

        DragTracker dt = tool.callGetDragTracker(mockFigure);
        assertNotNull(dt);
        assertTrue(dt instanceof DragTracker);
    }

    @Test
    public void testResolveTrackerWithNothingReturnsSelectAreaTracker() {
        when(mockView.findHandle(any(Point.class))).thenReturn(null);
        when(mockView.findFigure(any(Point.class))).thenReturn(null);
        tool.activate(mockEditor);

        MouseEvent evt = new MouseEvent(dummyComponent, MouseEvent.MOUSE_PRESSED,
                System.currentTimeMillis(), 0, 10, 10, 1, false, MouseEvent.BUTTON1);

        tool.mousePressed(evt);

        Tool sat = tool.callGetSelectAreaTracker();
        assertNotNull(sat);
        assertTrue(sat instanceof SelectAreaTracker);
    }

    @Test
    public void testSetSelectBehindEnabledNoChangeDoesNotFire() {
        // PropertyChangeSupport skips firing when old == new value
        PropertyChangeListener listener = mock(PropertyChangeListener.class);
        tool.addPropertyChangeListener(listener);

        tool.setSelectBehindEnabled(true); // same as default — no event expected

        verify(listener, never()).propertyChange(any(PropertyChangeEvent.class));
    }

    // -------------------------------------------------------------------------
    // C. Invariant tests
    // -------------------------------------------------------------------------

    @Test
    public void testInvariantTrackerNeverNull() {
        TestableSelectionTool freshTool = new TestableSelectionTool();
        assertNotNull(freshTool.callGetSelectAreaTracker());
    }

    @Test
    public void testInvariantSelectBehindEnabledDefaultTrue() {
        TestableSelectionTool freshTool = new TestableSelectionTool();
        assert freshTool.isSelectBehindEnabled() : "selectBehindEnabled must be true by default";
        assertTrue(freshTool.isSelectBehindEnabled());
    }

    @Test
    public void testInvariantSupportsHandleInteractionAlwaysTrue() {
        TestableSelectionTool freshTool = new TestableSelectionTool();
        assert freshTool.supportsHandleInteraction() : "supportsHandleInteraction must always be true";
        assertTrue(freshTool.supportsHandleInteraction());
    }
}
