/*
 * GroupFeatureBddTest.java
 *
 * BDD scenarios for the group/ungroup user story, automated with JGiven
 * (TestNG integration) and AssertJ assertions in the Then stage.
 */
package org.jhotdraw.draw.action.bdd;

import com.tngtech.jgiven.testng.ScenarioTest;
import org.testng.annotations.Test;

/**
 * User story: "As a JHotDraw user building a diagram, I want to combine
 * several selected figures into a single group ... and later ungroup
 * them back into individual figures."
 */
public class GroupFeatureBddTest
        extends ScenarioTest<GivenFigures, WhenGroupAction, ThenDrawing> {

    @Test
    public void grouping_combines_selected_figures_into_a_single_group() {
        given().a_drawing_with_two_selected_figures();
        when().the_user_groups_the_selection();
        then().the_drawing_contains_the_group()
                .and().the_figures_are_children_of_the_group()
                .and().the_figures_are_no_longer_top_level();
    }

    @Test
    public void ungrouping_restores_the_individual_figures() {
        given().a_drawing_with_a_group_of_two_figures();
        when().the_user_ungroups_the_group();
        then().the_figures_are_restored_to_the_drawing()
                .and().the_group_is_removed_from_the_drawing();
    }
}
