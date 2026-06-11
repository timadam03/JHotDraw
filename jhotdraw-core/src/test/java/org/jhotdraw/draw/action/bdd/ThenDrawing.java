/*
 * ThenDrawing.java — JGiven "Then" stage for the group/ungroup feature.
 */
package org.jhotdraw.draw.action.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import java.util.List;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.figure.Figure;
import org.jhotdraw.draw.figure.GroupFigure;

public class ThenDrawing extends Stage<ThenDrawing> {

    @ExpectedScenarioState
    Drawing drawing;
    @ExpectedScenarioState
    GroupFigure group;
    @ExpectedScenarioState
    List<Figure> figures;

    public ThenDrawing the_drawing_contains_the_group() {
        assertThat(drawing.contains(group)).isTrue();
        return self();
    }

    public ThenDrawing the_figures_are_children_of_the_group() {
        assertThat(group.getChildren()).containsExactlyElementsOf(figures);
        return self();
    }

    public ThenDrawing the_figures_are_no_longer_top_level() {
        for (Figure f : figures) {
            assertThat(drawing.contains(f)).isFalse();
        }
        return self();
    }

    public ThenDrawing the_figures_are_restored_to_the_drawing() {
        for (Figure f : figures) {
            assertThat(drawing.contains(f)).isTrue();
        }
        return self();
    }

    public ThenDrawing the_group_is_removed_from_the_drawing() {
        assertThat(drawing.contains(group)).isFalse();
        assertThat(group.getChildren()).isEmpty();
        return self();
    }
}
