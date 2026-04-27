package org.jhotdraw.draw.tool.bdd;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.BeforeStage;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.figure.Figure;
import org.jhotdraw.draw.handle.Handle;
import org.jhotdraw.draw.tool.DragTracker;
import org.jhotdraw.draw.tool.HandleTracker;
import org.jhotdraw.draw.tool.SelectAreaTracker;
import org.jhotdraw.draw.tool.SelectionTool;
import org.jhotdraw.draw.tool.Tool;

import javax.swing.JPanel;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

public class GivenSelectionTool extends Stage<GivenSelectionTool> {

    @ProvidedScenarioState
    TestableSelectionTool tool;

    @ProvidedScenarioState
    DrawingEditor mockEditor;

    @ProvidedScenarioState
    DrawingView mockView;

    @ProvidedScenarioState
    Drawing mockDrawing;

    @ProvidedScenarioState
    Figure mockFigure;

    @ProvidedScenarioState
    Handle mockHandle;

    @ProvidedScenarioState
    JPanel dummyComponent;

    @ProvidedScenarioState
    PropertyChangeListener mockListener;

    @BeforeStage
    public void setupMocks() {
        tool = new TestableSelectionTool();

        mockEditor = mock(DrawingEditor.class);
        mockView = mock(DrawingView.class);
        mockDrawing = mock(Drawing.class);
        mockFigure = mock(Figure.class);
        mockHandle = mock(Handle.class);
        dummyComponent = new JPanel();
        mockListener = mock(PropertyChangeListener.class);

        org.mockito.Mockito.when(mockEditor.getActiveView()).thenReturn(mockView);
        org.mockito.Mockito.when(mockEditor.findView(any(Container.class))).thenReturn(mockView);
        org.mockito.Mockito.when(mockEditor.getDrawingViews()).thenReturn(Collections.emptySet());

        org.mockito.Mockito.when(mockView.getDrawing()).thenReturn(mockDrawing);
        org.mockito.Mockito.when(mockView.isEnabled()).thenReturn(true);
        org.mockito.Mockito.when(mockView.viewToDrawing(any(Point.class))).thenReturn(new Point2D.Double(100, 100));
        org.mockito.Mockito.when(mockView.getSelectedFigures()).thenReturn(Collections.emptySet());
        org.mockito.Mockito.when(mockView.findHandle(any(Point.class))).thenReturn(null);
        org.mockito.Mockito.when(mockView.findFigure(any(Point.class))).thenReturn(null);
        org.mockito.Mockito.when(mockView.getCompatibleHandles(any())).thenReturn(Collections.emptyList());

        org.mockito.Mockito.when(mockHandle.getCursor()).thenReturn(Cursor.getDefaultCursor());
    }

    public GivenSelectionTool a_drawing_with_a_selectable_figure() {
        org.mockito.Mockito.when(mockView.findFigure(any(Point.class))).thenReturn(mockFigure);
        org.mockito.Mockito.when(mockFigure.isSelectable()).thenReturn(true);
        org.mockito.Mockito.when(mockFigure.contains(any(Point2D.Double.class))).thenReturn(false);
        tool.activate(mockEditor);
        return self();
    }

    public GivenSelectionTool a_drawing_with_a_handle_at_click_point() {
        org.mockito.Mockito.when(mockView.findHandle(any(Point.class))).thenReturn(mockHandle);
        org.mockito.Mockito.when(mockView.getCompatibleHandles(any(Handle.class))).thenReturn(Collections.singletonList(mockHandle));
        tool.activate(mockEditor);
        return self();
    }

    public GivenSelectionTool an_empty_drawing_area() {
        org.mockito.Mockito.when(mockView.findHandle(any(Point.class))).thenReturn(null);
        org.mockito.Mockito.when(mockView.findFigure(any(Point.class))).thenReturn(null);
        tool.activate(mockEditor);
        return self();
    }

    public GivenSelectionTool a_disabled_drawing_view() {
        org.mockito.Mockito.when(mockView.isEnabled()).thenReturn(false);
        tool.activate(mockEditor);
        return self();
    }

    public GivenSelectionTool overlapping_figures_with_select_behind_enabled() {
        Figure mockFigureBehind = mock(Figure.class);
        org.mockito.Mockito.when(mockFigureBehind.isSelectable()).thenReturn(true);
        org.mockito.Mockito.when(mockFigureBehind.contains(any(Point2D.Double.class))).thenReturn(false);

        org.mockito.Mockito.when(mockView.findFigure(any(Point.class))).thenReturn(mockFigure);
        org.mockito.Mockito.when(mockFigure.isSelectable()).thenReturn(true);
        org.mockito.Mockito.when(mockDrawing.findFigureBehind(any(Point2D.Double.class), any(Figure.class))).thenReturn(mockFigureBehind);

        tool.setSelectBehindEnabled(true);
        tool.activate(mockEditor);
        return self();
    }

    public GivenSelectionTool a_newly_created_SelectionTool() {
        tool = new TestableSelectionTool();
        return self();
    }

    public GivenSelectionTool the_SelectionTool_with_default_settings() {
        tool.activate(mockEditor);
        return self();
    }

    // -------------------------------------------------------------------------
    // Inner class exposing protected methods of SelectionTool for testing
    // -------------------------------------------------------------------------

    public static class TestableSelectionTool extends SelectionTool {

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
}
