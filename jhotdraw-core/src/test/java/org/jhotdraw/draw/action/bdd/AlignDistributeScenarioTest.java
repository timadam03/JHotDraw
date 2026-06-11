/*
 * AlignDistributeScenarioTest.java
 *
 * New file added for the align and distribute feature.
 * JHotDraw is distributed under the GNU LGPL v2.1.
 */
package org.jhotdraw.draw.action.bdd;

import com.tngtech.jgiven.junit.SimpleScenarioTest;
import org.jhotdraw.draw.action.Alignment;
import org.jhotdraw.draw.action.Distribution;
import org.junit.Test;

/**
 * BDD scenarios for the align and distribute user story.
 *
 * <p>This maps the user story onto runnable scenarios. The story reads, as
 * someone editing a drawing I want to align or distribute the figures I have
 * selected so the diagram looks tidy. Each test below is one acceptance
 * criterion written as Given a selection, When I run the command, Then the
 * figures end up where the story promises.</p>
 *
 * <p>Java note to self. Extending {@code SimpleScenarioTest<AlignDistributeStage>}
 * is what gives this class the {@code given()}, {@code when()}, and {@code then()}
 * methods, each returning the one stage so the steps can be chained. I used the
 * Simple variant because a single stage holds all three step groups.</p>
 */
public class AlignDistributeScenarioTest extends SimpleScenarioTest<AlignDistributeStage> {

    @Test
    public void aligning_to_the_north_lines_up_the_top_edges() {
        given().a_figure_at_x_$_y_$_with_width_$_height_$(0, 10, 20, 20)
                .and().a_figure_at_x_$_y_$_with_width_$_height_$(40, 60, 20, 20)
                .and().a_figure_at_x_$_y_$_with_width_$_height_$(80, 30, 20, 20)
                .and().the_figures_are_selected();
        when().I_align_them_to_the(Alignment.NORTH);
        then().every_figure_shares_the_same_top_edge();
    }

    @Test
    public void distributing_horizontally_makes_the_gaps_equal() {
        given().a_figure_at_x_$_y_$_with_width_$_height_$(0, 0, 10, 10)
                .and().a_figure_at_x_$_y_$_with_width_$_height_$(20, 0, 10, 10)
                .and().a_figure_at_x_$_y_$_with_width_$_height_$(100, 0, 10, 10)
                .and().the_figures_are_selected();
        when().I_distribute_them(Distribution.HORIZONTAL);
        then().the_gaps_between_neighbours_are_equal();
    }

    @Test
    public void distributing_two_figures_changes_nothing() {
        given().a_figure_at_x_$_y_$_with_width_$_height_$(0, 0, 10, 10)
                .and().a_figure_at_x_$_y_$_with_width_$_height_$(100, 0, 10, 10)
                .and().the_figures_are_selected();
        when().I_distribute_them(Distribution.HORIZONTAL);
        then().the_positions_are_unchanged();
    }
}
