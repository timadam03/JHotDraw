/*
 * ThenStacking.java — JGiven "Then" stage for stacking order.
 */
package org.jhotdraw.draw.action.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import java.util.List;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.figure.Figure;

public class ThenStacking extends Stage<ThenStacking> {

    @ExpectedScenarioState
    Drawing drawing;
    @ExpectedScenarioState
    Figure a;
    @ExpectedScenarioState
    Figure b;
    @ExpectedScenarioState
    Figure c;
    @ExpectedScenarioState
    Figure d;
    @ExpectedScenarioState
    List<Figure> originalOrder;

    public ThenStacking the_selected_figure_is_last_in_the_children_list() {
        assertThat(drawing.getChildren()).containsExactly(a, c, d, b);
        assertThat(drawing.indexOf(b)).isEqualTo(drawing.getChildCount() - 1);
        return self();
    }

    public ThenStacking the_selected_figure_is_first_in_the_children_list() {
        assertThat(drawing.getChildren()).containsExactly(c, a, b, d);
        assertThat(drawing.indexOf(c)).isEqualTo(0);
        return self();
    }

    public ThenStacking the_selected_figure_has_moved_one_layer_forward() {
        assertThat(drawing.getChildren()).containsExactly(a, c, b, d);
        assertThat(drawing.indexOf(b)).isEqualTo(2);
        return self();
    }

    public ThenStacking the_selected_figure_has_moved_one_layer_backward() {
        assertThat(drawing.getChildren()).containsExactly(a, c, b, d);
        assertThat(drawing.indexOf(c)).isEqualTo(1);
        return self();
    }

    public ThenStacking the_previous_order_is_restored() {
        assertThat(drawing.getChildren()).containsExactlyElementsOf(originalOrder);
        assertThat(drawing.indexOf(a)).isEqualTo(0);
        assertThat(drawing.indexOf(b)).isEqualTo(1);
        assertThat(drawing.indexOf(c)).isEqualTo(2);
        assertThat(drawing.indexOf(d)).isEqualTo(3);
        return self();
    }

    public ThenStacking unselected_figures_stay_put() {
        assertThat(drawing.getChildren()).containsExactly(a, c, b, d);
        assertThat(drawing.indexOf(a)).isEqualTo(0);
        assertThat(drawing.indexOf(d)).isEqualTo(3);
        return self();
    }
}
