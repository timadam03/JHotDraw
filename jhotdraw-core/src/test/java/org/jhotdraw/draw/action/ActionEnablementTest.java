/* @(#)ActionEnablementTest.java
 *
 * Copyright (c) the original authors of JHotDraw and all its contributors.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.draw.action;

import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Action-level tests for the enabled-state rules of the align and distribute
 * actions. The Swing dependency is a mocked {@link DrawingView}, reached through
 * a mocked {@link DrawingEditor}, so the rules can be checked without a live
 * editor. This is the test double the TestLab asks for, and it covers the
 * acceptance criteria the pure-geometry scenarios do not, the enable-at-two rule
 * for align and the enable-at-three rule for distribute.
 */
public class ActionEnablementTest {

    private DrawingEditor editor;
    private DrawingView view;

    @Before
    public void setUp() {
        editor = mock(DrawingEditor.class);
        view = mock(DrawingView.class);
        when(editor.getActiveView()).thenReturn(view);
        when(view.isEnabled()).thenReturn(true);
    }

    @Test
    public void alignIsDisabledWithOneFigureAndEnabledWithTwo() {
        AlignAction.North align = new AlignAction.North(editor);

        when(view.getSelectionCount()).thenReturn(1);
        align.updateEnabledState();
        assertFalse("align needs at least two figures", align.isEnabled());

        when(view.getSelectionCount()).thenReturn(2);
        align.updateEnabledState();
        assertTrue("align is enabled at two figures", align.isEnabled());
    }

    @Test
    public void distributeIsDisabledWithTwoFiguresAndEnabledWithThree() {
        DistributeAction.Horizontal distribute = new DistributeAction.Horizontal(editor);

        when(view.getSelectionCount()).thenReturn(2);
        distribute.updateEnabledState();
        assertFalse("distribute needs at least three figures", distribute.isEnabled());

        when(view.getSelectionCount()).thenReturn(3);
        distribute.updateEnabledState();
        assertTrue("distribute is enabled at three figures", distribute.isEnabled());
    }
}
