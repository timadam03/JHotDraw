package org.jhotdraw.draw.tool.bdd;

import com.tngtech.jgiven.junit.ScenarioTest;
import org.junit.Test;

/**
 * BDD tests for {@link org.jhotdraw.draw.tool.SelectionTool} using JGiven.
 * Each test maps to a User Story / BDD scenario for the SelectionTool feature.
 */
public class SelectionToolBDDTest
        extends ScenarioTest<GivenSelectionTool, WhenSelectionTool, ThenSelectionTool> {

    // US1: As a drawing user, I want to select a figure by clicking on it
    @Test
    public void clicking_on_a_selectable_figure_activates_drag_tracker() {
        given().a_drawing_with_a_selectable_figure();
        when().the_user_clicks_on_the_figure();
        then().the_DragTracker_is_activated();
    }

    // US3: As a drawing user, I want to manipulate figure handles
    @Test
    public void clicking_on_a_handle_activates_handle_tracker() {
        given().a_drawing_with_a_handle_at_click_point();
        when().the_user_presses_mouse_on_handle();
        then().the_HandleTracker_is_activated();
    }

    // US4: As a drawing user, I want to select an area by rubber-banding
    @Test
    public void clicking_on_empty_area_activates_select_area_tracker() {
        given().an_empty_drawing_area();
        when().the_user_presses_mouse_on_empty_area();
        then().the_SelectAreaTracker_is_activated();
    }

    // Boundary: disabled view should not trigger tracker
    @Test
    public void disabled_view_prevents_tracker_action() {
        given().a_disabled_drawing_view();
        when().the_user_presses_mouse_with_disabled_view();
        then().no_tracker_action_is_performed();
    }

    // US5: As a drawing user, I want to select a figure behind another
    @Test
    public void alt_click_with_overlapping_figures_selects_figure_behind() {
        given().overlapping_figures_with_select_behind_enabled();
        when().the_user_clicks_with_alt_modifier();
        then().the_DragTracker_is_activated();
    }

    // Property change: selectBehindEnabled fires event
    @Test
    public void setting_selectBehindEnabled_fires_property_change() {
        given().the_SelectionTool_with_default_settings();
        when().selectBehindEnabled_is_set_to(false);
        then().selectBehindEnabled_is(false);
    }

    // Invariant: lazy initialization returns same instance
    @Test
    public void select_area_tracker_lazy_init_returns_same_instance() {
        given().a_newly_created_SelectionTool();
        when().a_tracker_is_requested_multiple_times();
        then().the_same_tracker_instance_is_returned();
    }

    @Test
    public void drag_tracker_lazy_init_returns_same_instance() {
        given().a_newly_created_SelectionTool();
        when().a_drag_tracker_is_requested_multiple_times();
        then().the_same_tracker_instance_is_returned();
    }

    @Test
    public void handle_tracker_lazy_init_returns_same_instance() {
        given().a_newly_created_SelectionTool();
        when().a_handle_tracker_is_requested_multiple_times();
        then().the_same_tracker_instance_is_returned();
    }

    // Invariant: supports handle interaction is always true
    @Test
    public void selection_tool_always_supports_handle_interaction() {
        given().a_newly_created_SelectionTool();
        when().selectBehindEnabled_is_set_to(true);
        then().the_tool_supports_handle_interaction();
    }
}
