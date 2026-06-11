/*
 * WhenGroupAction.java — JGiven "When" stage for the group/ungroup feature.
 */
package org.jhotdraw.draw.action.bdd;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import java.util.List;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.action.GroupAction;
import org.jhotdraw.draw.figure.Figure;
import org.jhotdraw.draw.figure.GroupFigure;

public class WhenGroupAction extends Stage<WhenGroupAction> {

    @ExpectedScenarioState
    GroupAction action;
    @ExpectedScenarioState
    DrawingView view;
    @ExpectedScenarioState
    GroupFigure group;
    @ExpectedScenarioState
    List<Figure> figures;

    public WhenGroupAction the_user_groups_the_selection() {
        action.groupFigures(view, group, figures);
        return self();
    }

    public WhenGroupAction the_user_ungroups_the_group() {
        action.ungroupFigures(view, group);
        return self();
    }
}
