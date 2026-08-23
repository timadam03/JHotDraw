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
    List<Figure> originalOrder;

    private Figure orig(int i) {
        return originalOrder.get(i);
    }

    public ThenStacking the_selected_figure_is_last_in_the_children_list() {
        assertThat(drawing.getChildren()).containsExactly(orig(0), orig(2), orig(3), orig(1));
        assertThat(drawing.indexOf(orig(1))).isEqualTo(drawing.getChildCount() - 1);
        return self();
    }

    public ThenStacking the_selected_figure_is_first_in_the_children_list() {
        assertThat(drawing.getChildren()).containsExactly(orig(2), orig(0), orig(1), orig(3));
        assertThat(drawing.indexOf(orig(2))).isEqualTo(0);
        return self();
    }

    public ThenStacking the_selected_figure_has_moved_one_layer_forward() {
        assertThat(drawing.getChildren()).containsExactly(orig(0), orig(2), orig(1), orig(3));
        assertThat(drawing.indexOf(orig(1))).isEqualTo(2);
        return self();
    }

    public ThenStacking the_selected_figure_has_moved_one_layer_backward() {
        assertThat(drawing.getChildren()).containsExactly(orig(0), orig(2), orig(1), orig(3));
        assertThat(drawing.indexOf(orig(2))).isEqualTo(1);
        return self();
    }

    public ThenStacking the_previous_order_is_restored() {
        assertThat(drawing.getChildren()).containsExactlyElementsOf(originalOrder);
        return self();
    }

    public ThenStacking unselected_figures_stay_put() {
        assertThat(drawing.getChildren()).containsExactly(orig(0), orig(2), orig(1), orig(3));
        assertThat(drawing.indexOf(orig(0))).isEqualTo(0);
        assertThat(drawing.indexOf(orig(3))).isEqualTo(3);
        return self();
    }
}
