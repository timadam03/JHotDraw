/*
 * StackingFeatureBddTest.java
 *
 * BDD scenarios for stacking order, automated with JGiven
 * (TestNG integration) and AssertJ assertions in the Then stage.
 */
package org.jhotdraw.draw.action.bdd;

import com.tngtech.jgiven.testng.ScenarioTest;
import org.testng.annotations.Test;

/**
 * User story: bring a figure to front or back, or one layer forward / backward.
 * Child list: index 0 is back, last is front. Undo restores the previous order.
 */
public class StackingFeatureBddTest
        extends ScenarioTest<GivenStacking, WhenStacking, ThenStacking> {

    @Test
    public void bringing_a_figure_to_front_puts_it_last_in_the_children_list() {
        given().a_drawing_with_four_stacked_figures()
                .and().the_second_figure_is_selected();
        when().the_user_brings_the_selection_to_front();
        then().the_selected_figure_is_last_in_the_children_list();
    }

    @Test
    public void sending_a_figure_to_back_puts_it_first() {
        given().a_drawing_with_four_stacked_figures()
                .and().the_third_figure_is_selected();
        when().the_user_sends_the_selection_to_back();
        then().the_selected_figure_is_first_in_the_children_list();
    }

    @Test
    public void bring_forward_moves_one_layer() {
        given().a_drawing_with_four_stacked_figures()
                .and().the_second_figure_is_selected();
        when().the_user_brings_the_selection_forward();
        then().the_selected_figure_has_moved_one_layer_forward();
    }

    @Test
    public void send_backward_moves_one_layer() {
        given().a_drawing_with_four_stacked_figures()
                .and().the_third_figure_is_selected();
        when().the_user_sends_the_selection_backward();
        then().the_selected_figure_has_moved_one_layer_backward();
    }

    @Test
    public void undo_restores_the_previous_order() {
        given().a_drawing_with_four_stacked_figures()
                .and().the_second_figure_is_selected();
        when().the_bring_to_front_action_is_performed()
                .and().the_user_undoes();
        then().the_previous_order_is_restored();
    }

    @Test
    public void unselected_figures_stay_put() {
        given().a_drawing_with_four_stacked_figures()
                .and().the_second_figure_is_selected();
        when().the_user_brings_the_selection_forward();
        then().unselected_figures_stay_put();
    }
}
