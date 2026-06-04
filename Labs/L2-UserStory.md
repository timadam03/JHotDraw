# L2 — Change Request: User Story (Selection Tool)

For my individual portfolio I picked the **Selection Tool** (`org.jhotdraw.draw.tool.SelectionTool`, in `jhotdraw-core`). It is the tool the user is in when they interact directly with the canvas to pick figures and move them around, so it is the most user-facing piece of behaviour I could attach a clear story to.

## Story

> **As a** person drawing a diagram in the SVG editor,
> **I want** to select and manipulate the figures I have already placed on the canvas,
> **so that** I can move, resize and rearrange my drawing without redrawing it from scratch.

This is tracked on the team's GitHub backlog (we keep the backlog as GitHub issues) as **issue #12**, labelled **`user story`**, currently sitting in the **TODO** column: https://github.com/timadam03/JHotDraw/issues/12

## Acceptance criteria

These line up one-to-one with the behaviours I later drove out in the BDD scenarios (US1-US5).

| # | Given / When | Then |
|---|--------------|------|
| US1 | A selectable figure exists and I click on it | The figure becomes the drag target and I can drag to move it (`DragTracker`) |
| US2 | I click on an empty part of the canvas | The current selection is cleared |
| US3 | I click directly on a handle of a selected figure | I can drag that handle to resize/transform the figure (`HandleTracker`) |
| US4 | I press and drag starting on empty canvas | A rubber-band selection rectangle is drawn over the area (`SelectAreaTracker`) |
| US5 | Two figures overlap and I hold Alt/Ctrl while clicking | The figure *behind* the front one is selected (select-behind) |

## Notes / scope

- The story is about the existing user-visible behaviour; I am not adding a new feature here. The selection of this story is what frames the later refactoring (branch `altan/develop`) and the unit + BDD tests on the same feature.
- "Selectable" follows `Figure.isSelectable()`; deselect and select-behind only apply when the view is active, so the criteria assume an enabled `DrawingView`.
- US5 is gated by the bound `selectBehindEnabled` property (default on), which is why the modifier-key behaviour is listed as a criterion rather than always-on.
