/*
 * WhenStacking.java — JGiven "When" stage for stacking order.
 */
package org.jhotdraw.draw.action.bdd;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import java.awt.event.ActionEvent;
import javax.swing.undo.UndoManager;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.action.BringForwardAction;
import org.jhotdraw.draw.action.BringToFrontAction;
import org.jhotdraw.draw.action.SendBackwardAction;
import org.jhotdraw.draw.action.SendToBackAction;

public class WhenStacking extends Stage<WhenStacking> {

    @ExpectedScenarioState
    DrawingView view;
    @ExpectedScenarioState
    DrawingEditor editor;
    @ExpectedScenarioState
    UndoManager undoManager;

    public WhenStacking the_user_brings_the_selection_to_front() {
        BringToFrontAction.bringToFront(view, view.getSelectedFigures());
        return self();
    }

    public WhenStacking the_user_sends_the_selection_to_back() {
        SendToBackAction.sendToBack(view, view.getSelectedFigures());
        return self();
    }

    public WhenStacking the_user_brings_the_selection_forward() {
        BringForwardAction.bringForward(view, view.getSelectedFigures());
        return self();
    }

    public WhenStacking the_user_sends_the_selection_backward() {
        SendBackwardAction.sendBackward(view, view.getSelectedFigures());
        return self();
    }

    public WhenStacking the_bring_to_front_action_is_performed() {
        // actionPerformed fires the undoable edit; the static helpers do not
        BringToFrontAction action = new BringToFrontAction(editor);
        action.actionPerformed(new ActionEvent(action, ActionEvent.ACTION_PERFORMED, BringToFrontAction.ID));
        return self();
    }

    public WhenStacking the_user_undoes() {
        undoManager.undo();
        return self();
    }
}
