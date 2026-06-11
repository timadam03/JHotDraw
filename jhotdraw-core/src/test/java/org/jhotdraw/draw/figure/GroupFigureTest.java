/*
 * GroupFigureTest.java
 *
 * Unit tests for the Composite child-management behaviour that the
 * group/ungroup feature relies on.
 */
package org.jhotdraw.draw.figure;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link GroupFigure} / {@link AbstractCompositeFigure} child
 * management, which is the core of the group and ungroup feature.
 */
public class GroupFigureTest {

    private GroupFigure group;
    private RectangleFigure rectA;
    private RectangleFigure rectB;

    @Before
    public void setUp() {
        group = new GroupFigure();
        rectA = new RectangleFigure(0, 0, 10, 10);
        rectB = new RectangleFigure(20, 20, 10, 10);
    }

    // --- boundary case: a brand-new group is empty ---
    @Test
    public void newGroupHasNoChildren() {
        assertTrue("a new group should start empty", group.getChildren().isEmpty());
    }

    // --- best case: adding a child is reflected in getChildren() ---
    @Test
    public void basicAddPutsFigureIntoGroup() {
        group.basicAdd(rectA);
        assertEquals(1, group.getChildren().size());
        assertTrue(group.getChildren().contains(rectA));
    }

    // --- best case: multiple children kept in insertion order ---
    @Test
    public void getChildrenReflectsMultipleAdds() {
        group.basicAdd(rectA);
        group.basicAdd(rectB);
        assertEquals(2, group.getChildren().size());
        assertEquals(rectA, group.getChildren().get(0));
        assertEquals(rectB, group.getChildren().get(1));
    }

    // --- the ungroup core: removing all children empties the group ---
    @Test
    public void basicRemoveAllChildrenEmptiesGroup() {
        group.basicAdd(rectA);
        group.basicAdd(rectB);
        group.basicRemoveAllChildren();
        assertTrue("group should be empty after removing all children",
                group.getChildren().isEmpty());
    }

    // --- round trip: group then ungroup preserves the member figures ---
    @Test
    public void groupThenUngroupPreservesMembers() {
        group.basicAdd(rectA);
        group.basicAdd(rectB);

        // snapshot the members into a new list (getChildren() is a live
        // view, so this mirrors what ungroupFigures does before clearing)
        List<Figure> members = new ArrayList<>(group.getChildren());
        assertEquals(2, members.size());

        group.basicRemoveAllChildren();

        // acceptance criterion 5: members survive ungrouping unchanged
        assertTrue(members.contains(rectA));
        assertTrue(members.contains(rectB));
        assertFalse("members must no longer be owned by the group",
                group.getChildren().contains(rectA));
    }
}
