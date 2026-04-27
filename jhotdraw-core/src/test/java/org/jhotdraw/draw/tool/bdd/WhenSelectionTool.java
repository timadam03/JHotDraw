package org.jhotdraw.draw.tool.bdd;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.ScenarioState.Resolution;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.figure.Figure;
import org.jhotdraw.draw.handle.Handle;
import org.jhotdraw.draw.tool.Tool;

import javax.swing.JPanel;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;

public class WhenSelectionTool extends Stage<WhenSelectionTool> {

    @ExpectedScenarioState
    GivenSelectionTool.TestableSelectionTool tool;

    @ExpectedScenarioState
    DrawingEditor mockEditor;

    @ExpectedScenarioState
    DrawingView mockView;

    @ExpectedScenarioState
    Drawing mockDrawing;

    @ExpectedScenarioState
    Figure mockFigure;

    @ExpectedScenarioState
    Handle mockHandle;

    @ExpectedScenarioState
    JPanel dummyComponent;

    @ExpectedScenarioState
    PropertyChangeListener mockListener;

    @ProvidedScenarioState
    MouseEvent lastMouseEvent;

    @ProvidedScenarioState(resolution = Resolution.NAME)
    Tool firstTrackerInstance;

    @ProvidedScenarioState(resolution = Resolution.NAME)
    Tool secondTrackerInstance;

    public WhenSelectionTool the_user_clicks_on_the_figure() {
        MouseEvent evt = createMouseEvent(0);
        tool.mousePressed(evt);
        lastMouseEvent = evt;
        return self();
    }

    public WhenSelectionTool the_user_presses_mouse_on_handle() {
        MouseEvent evt = createMouseEvent(0);
        tool.mousePressed(evt);
        lastMouseEvent = evt;
        return self();
    }

    public WhenSelectionTool the_user_presses_mouse_on_empty_area() {
        MouseEvent evt = createMouseEvent(0);
        tool.mousePressed(evt);
        lastMouseEvent = evt;
        return self();
    }

    public WhenSelectionTool the_user_presses_mouse_with_disabled_view() {
        MouseEvent evt = createMouseEvent(0);
        tool.mousePressed(evt);
        lastMouseEvent = evt;
        return self();
    }

    public WhenSelectionTool the_user_clicks_with_alt_modifier() {
        MouseEvent evt = createMouseEvent(InputEvent.ALT_DOWN_MASK);
        tool.mousePressed(evt);
        lastMouseEvent = evt;
        return self();
    }

    public WhenSelectionTool selectBehindEnabled_is_set_to(boolean value) {
        tool.setSelectBehindEnabled(value);
        return self();
    }

    public WhenSelectionTool a_tracker_is_requested_multiple_times() {
        firstTrackerInstance = tool.callGetSelectAreaTracker();
        secondTrackerInstance = tool.callGetSelectAreaTracker();
        return self();
    }

    public WhenSelectionTool a_drag_tracker_is_requested_multiple_times() {
        firstTrackerInstance = tool.callGetDragTracker(mockFigure);
        secondTrackerInstance = tool.callGetDragTracker(mockFigure);
        return self();
    }

    public WhenSelectionTool a_handle_tracker_is_requested_multiple_times() {
        tool.activate(mockEditor);
        firstTrackerInstance = tool.callGetHandleTracker(mockHandle);
        secondTrackerInstance = tool.callGetHandleTracker(mockHandle);
        return self();
    }

    private MouseEvent createMouseEvent(int modifiers) {
        return new MouseEvent(dummyComponent, MouseEvent.MOUSE_PRESSED,
                System.currentTimeMillis(), modifiers, 10, 10, 1, false, MouseEvent.BUTTON1);
    }
}
