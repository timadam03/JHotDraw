/*
 * GivenStacking.java — JGiven "Given" stage for stacking order.
 */
package org.jhotdraw.draw.action.bdd;

import static org.mockito.Mockito.mock;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.undo.UndoManager;
import org.jhotdraw.draw.DefaultDrawing;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.figure.Figure;
import org.jhotdraw.draw.figure.RectangleFigure;

public class GivenStacking extends Stage<GivenStacking> {

    @ProvidedScenarioState
    Drawing drawing;
    @ProvidedScenarioState
    DrawingView view;
    @ProvidedScenarioState
    DrawingEditor editor;
    Figure a;
    Figure b;
    Figure c;
    Figure d;
    @ProvidedScenarioState
    List<Figure> originalOrder;
    @ProvidedScenarioState
    UndoManager undoManager;

    public GivenStacking a_drawing_with_four_stacked_figures() {
        editor = mock(DrawingEditor.class);
        view = mock(DrawingView.class);

        drawing = new DefaultDrawing();
        a = new RectangleFigure(0, 0, 10, 10);
        b = new RectangleFigure(20, 0, 10, 10);
        c = new RectangleFigure(40, 0, 10, 10);
        d = new RectangleFigure(60, 0, 10, 10);
        drawing.add(a);
        drawing.add(b);
        drawing.add(c);
        drawing.add(d);
        originalOrder = new ArrayList<>(drawing.getChildren());

        undoManager = new UndoManager();
        drawing.addUndoableEditListener(undoManager);

        org.mockito.Mockito.when(editor.getActiveView()).thenReturn(view);
        org.mockito.Mockito.when(view.getDrawing()).thenReturn(drawing);
        org.mockito.Mockito.when(view.isEnabled()).thenReturn(true);
        select();
        return self();
    }

    public GivenStacking the_second_figure_is_selected() {
        select(b);
        return self();
    }

    public GivenStacking the_third_figure_is_selected() {
        select(c);
        return self();
    }

    private void select(Figure... figures) {
        Set<Figure> selected = new LinkedHashSet<>();
        Collections.addAll(selected, figures);
        org.mockito.Mockito.when(view.getSelectedFigures()).thenReturn(selected);
        org.mockito.Mockito.when(view.getSelectionCount()).thenReturn(selected.size());
    }
}
