/*
 * GroupActionTest.java
 *
 * Unit tests for the group/ungroup operations in GroupAction, using a
 * mocked DrawingView (the dependency) over a real DefaultDrawing model.
 */
package org.jhotdraw.draw.action;

import java.util.ArrayList;
import java.util.List;
import org.jhotdraw.draw.DefaultDrawing;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.figure.Figure;
import org.jhotdraw.draw.figure.GroupFigure;
import org.jhotdraw.draw.figure.RectangleFigure;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link GroupAction#groupFigures} and
 * {@link GroupAction#ungroupFigures}. The {@link DrawingView} is mocked
 * so the test exercises a single code path through the action method;
 * the {@link Drawing} model is real so the structural result is genuine.
 */
public class GroupActionTest {

    private GroupAction action;
    private Drawing drawing;
    private DrawingView view;
    private GroupFigure group;
    private RectangleFigure rectA;
    private RectangleFigure rectB;
    private List<Figure> figures;

    @Before
    public void setUp() {
        DrawingEditor editor = mock(DrawingEditor.class);
        action = new GroupAction(editor);

        drawing = new DefaultDrawing();
        rectA = new RectangleFigure(0, 0, 10, 10);
        rectB = new RectangleFigure(20, 20, 10, 10);
        drawing.add(rectA);
        drawing.add(rectB);

        group = new GroupFigure();
        figures = new ArrayList<>();
        figures.add(rectA);
        figures.add(rectB);

        // the only stubbed dependency: the view hands back the real drawing
        view = mock(DrawingView.class);
        when(view.getDrawing()).thenReturn(drawing);
    }

    // --- best case: grouping moves the figures under the group ---
    @Test
    public void groupFiguresMakesFiguresChildrenAndRemovesThemFromDrawing() {
        action.groupFigures(view, group, figures);

        // the group is now a top-level figure in the drawing
        assertTrue("drawing should contain the new group", drawing.contains(group));
        // both figures are now children of the group
        assertEquals(2, group.getChildren().size());
        assertTrue(group.getChildren().contains(rectA));
        assertTrue(group.getChildren().contains(rectB));
        // and are no longer top-level figures of the drawing
        assertFalse("rectA should no longer be top-level", drawing.contains(rectA));
        assertFalse("rectB should no longer be top-level", drawing.contains(rectB));
    }

    // --- round trip: ungroup restores the figures to the drawing ---
    @Test
    public void ungroupFiguresRestoresFiguresToDrawing() {
        action.groupFigures(view, group, figures);   // arrange: grouped state
        action.ungroupFigures(view, group);          // act: ungroup

        // figures are back as top-level figures
        assertTrue("rectA should be restored to the drawing", drawing.contains(rectA));
        assertTrue("rectB should be restored to the drawing", drawing.contains(rectB));
        // the group itself is gone
        assertFalse("the empty group should be removed", drawing.contains(group));
        assertTrue("the group should hold no children after ungroup",
                group.getChildren().isEmpty());
    }
}
