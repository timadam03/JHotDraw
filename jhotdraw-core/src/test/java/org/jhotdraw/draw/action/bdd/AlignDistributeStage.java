/*
 * AlignDistributeStage.java
 *
 * New file added for the align and distribute feature.
 * JHotDraw is distributed under the GNU LGPL v2.1.
 */
package org.jhotdraw.draw.action.bdd;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.jhotdraw.draw.action.Alignment;
import org.jhotdraw.draw.action.Distribution;

/**
 * A single JGiven stage holding the Given, When, and Then steps for the align
 * and distribute scenarios.
 *
 * <p>Where a unit test speaks in coordinates, BDD describes the feature in the
 * words of the user story. Each public method below is one line of a
 * Given-When-Then sentence, and JGiven turns the method names into a readable
 * report. The stage holds the scenario state, the figures and the result, so the
 * scenario class itself stays short and declarative.</p>
 *
 * <p>Java notes to self. A JGiven stage is a normal class whose methods return
 * {@code this} so they can be chained. {@code @ProvidedScenarioState} marks the
 * fields JGiven shares between the steps. {@code @Quoted} just makes the argument
 * show up in quotes in the generated report. One stage is enough here because
 * the feature is small, a bigger feature would split Given, When, and Then into
 * three stages.</p>
 */
public class AlignDistributeStage extends Stage<AlignDistributeStage> {

    @ProvidedScenarioState
    private final List<Rectangle2D.Double> figures = new ArrayList<>();

    @ProvidedScenarioState
    private Rectangle2D.Double selectionBounds;

    @ProvidedScenarioState
    private final List<Point2D.Double> alignDeltas = new ArrayList<>();

    @ProvidedScenarioState
    private double[] distributedPositions;

    // ---- Given ----

    public AlignDistributeStage a_figure_at_x_$_y_$_with_width_$_height_$(
            @Quoted double x, @Quoted double y, @Quoted double w, @Quoted double h) {
        figures.add(new Rectangle2D.Double(x, y, w, h));
        return this;
    }

    public AlignDistributeStage the_figures_are_selected() {
        selectionBounds = null;
        for (Rectangle2D.Double f : figures) {
            if (selectionBounds == null) {
                selectionBounds = (Rectangle2D.Double) f.clone();
            } else {
                selectionBounds.add(f);
            }
        }
        return this;
    }

    // ---- When ----

    public AlignDistributeStage I_align_them_to_the(@Quoted Alignment alignment) {
        alignDeltas.clear();
        for (Rectangle2D.Double f : figures) {
            alignDeltas.add(alignment.delta(f, selectionBounds));
        }
        return this;
    }

    public AlignDistributeStage I_distribute_them(@Quoted Distribution distribution) {
        distributedPositions = distribution.newPositions(figures);
        return this;
    }

    // ---- Then ----

    public AlignDistributeStage every_figure_shares_the_same_top_edge() {
        // Applying the NORTH delta should land every figure on the same y.
        List<Double> tops = new ArrayList<>();
        for (int i = 0; i < figures.size(); i++) {
            tops.add(figures.get(i).y + alignDeltas.get(i).y);
        }
        Assertions.assertThat(tops).allMatch(t -> Math.abs(t - tops.get(0)) < 1e-9);
        return this;
    }

    public AlignDistributeStage the_gaps_between_neighbours_are_equal() {
        // Sort the new leading coordinates and check the gaps all match.
        double[] sorted = distributedPositions.clone();
        java.util.Arrays.sort(sorted);
        double firstGap = sorted[1] - sorted[0];
        for (int i = 2; i < sorted.length; i++) {
            Assertions.assertThat(sorted[i] - sorted[i - 1]).isCloseTo(firstGap, Assertions.within(1e-9));
        }
        return this;
    }

    public AlignDistributeStage the_positions_are_unchanged() {
        for (int i = 0; i < figures.size(); i++) {
            Assertions.assertThat(distributedPositions[i]).isCloseTo(figures.get(i).x, Assertions.within(1e-9));
        }
        return this;
    }
}
