package org.jhotdraw.draw.tool.bdd;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ScenarioState.Resolution;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.figure.Figure;
import org.jhotdraw.draw.handle.Handle;
import org.jhotdraw.draw.tool.DragTracker;
import org.jhotdraw.draw.tool.HandleTracker;
import org.jhotdraw.draw.tool.SelectAreaTracker;
import org.jhotdraw.draw.tool.Tool;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class ThenSelectionTool extends Stage<ThenSelectionTool> {

    @ExpectedScenarioState
    GivenSelectionTool.TestableSelectionTool tool;

    @ExpectedScenarioState
    DrawingEditor mockEditor;

    @ExpectedScenarioState
    DrawingView mockView;

    @ExpectedScenarioState
    Figure mockFigure;

    @ExpectedScenarioState
    Handle mockHandle;

    @ExpectedScenarioState
    PropertyChangeListener mockListener;

    @ExpectedScenarioState
    MouseEvent lastMouseEvent;

    @ExpectedScenarioState(resolution = Resolution.NAME)
    Tool firstTrackerInstance;

    @ExpectedScenarioState(resolution = Resolution.NAME)
    Tool secondTrackerInstance;

    public ThenSelectionTool the_DragTracker_is_activated() {
        Tool tracker = tool.callGetDragTracker(mockFigure);
        assertThat(tracker).isNotNull();
        assertThat(tracker).isInstanceOf(DragTracker.class);
        return self();
    }

    public ThenSelectionTool the_HandleTracker_is_activated() {
        Tool tracker = tool.callGetHandleTracker(mockHandle);
        assertThat(tracker).isNotNull();
        assertThat(tracker).isInstanceOf(HandleTracker.class);
        return self();
    }

    public ThenSelectionTool the_SelectAreaTracker_is_activated() {
        Tool tracker = tool.callGetSelectAreaTracker();
        assertThat(tracker).isNotNull();
        assertThat(tracker).isInstanceOf(SelectAreaTracker.class);
        return self();
    }

    public ThenSelectionTool no_tracker_action_is_performed() {
        verify(mockView, never()).findHandle(any(Point.class));
        return self();
    }

    public ThenSelectionTool the_property_change_event_is_fired() {
        verify(mockListener).propertyChange(any(PropertyChangeEvent.class));
        return self();
    }

    public ThenSelectionTool the_property_change_event_is_not_fired() {
        verify(mockListener, never()).propertyChange(any(PropertyChangeEvent.class));
        return self();
    }

    public ThenSelectionTool the_same_tracker_instance_is_returned() {
        assertThat(firstTrackerInstance).isNotNull();
        assertThat(secondTrackerInstance).isNotNull();
        assertThat(firstTrackerInstance).isSameAs(secondTrackerInstance);
        return self();
    }

    public ThenSelectionTool selectBehindEnabled_is(boolean expected) {
        assertThat(tool.isSelectBehindEnabled()).isEqualTo(expected);
        return self();
    }

    public ThenSelectionTool the_tool_supports_handle_interaction() {
        assertThat(tool.supportsHandleInteraction()).isTrue();
        return self();
    }
}
