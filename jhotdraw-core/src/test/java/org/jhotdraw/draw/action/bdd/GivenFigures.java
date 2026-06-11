/*
 * GivenFigures.java — JGiven "Given" stage for the group/ungroup feature.
 */
package org.jhotdraw.draw.action.bdd;

import static org.mockito.Mockito.mock;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import java.util.ArrayList;
import java.util.List;
import org.jhotdraw.draw.DefaultDrawing;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.action.GroupAction;
import org.jhotdraw.draw.figure.Figure;
import org.jhotdraw.draw.figure.GroupFigure;
import org.jhotdraw.draw.figure.RectangleFigure;

public class GivenFigures extends Stage<GivenFigures> {

    @ProvidedScenarioState
    Drawing drawing;
    @ProvidedScenarioState
    DrawingView view;
    @ProvidedScenarioState
    GroupAction action;
    @ProvidedScenarioState
    GroupFigure group;
    @ProvidedScenarioState
    List<Figure> figures;

    private void buildDrawingWithTwoFigures() {
        DrawingEditor editor = mock(DrawingEditor.class);
        action = new GroupAction(editor);

        drawing = new DefaultDrawing();
        RectangleFigure rectA = new RectangleFigure(0, 0, 10, 10);
        RectangleFigure rectB = new RectangleFigure(20, 20, 10, 10);
        drawing.add(rectA);
        drawing.add(rectB);

        figures = new ArrayList<>();
        figures.add(rectA);
        figures.add(rectB);

        group = new GroupFigure();

        view = mock(DrawingView.class);
        org.mockito.Mockito.when(view.getDrawing()).thenReturn(drawing);
    }

    public GivenFigures a_drawing_with_two_selected_figures() {
        buildDrawingWithTwoFigures();
        return self();
    }

    public GivenFigures a_drawing_with_a_group_of_two_figures() {
        buildDrawingWithTwoFigures();
        action.groupFigures(view, group, figures);
        return self();
    }
}
