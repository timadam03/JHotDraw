# Lab 4 — Refactoring the Selection Tool

The feature I have been working on all semester is `org.jhotdraw.draw.tool.SelectionTool` (module `jhotdraw-core`). It extends `AbstractTool` and is the tool the user drives on the canvas: it decides, on every mouse press, whether the user is grabbing a handle, dragging a figure, or rubber-banding a selection rectangle, and delegates the rest of the gesture to one of three tracker strategies (`HandleTracker`, `DragTracker`, `SelectAreaTracker`).

This lab covers the smells I found in that class, the plan I drew up, and the two behaviour-preserving commits I used to clean it up: `8243c188` ("Refactor SelectionTool: simplify tracker logic") and `35f97d94` ("Refactor SelectionTool target-figure lookup").

## How I found the smells

I read the class top to bottom first, then ran SonarLint over it in the IDE. SonarLint is an IDE plugin, so there is no ruleset committed to the repo — the warnings live in the editor, not in CI. The two it kept raising were:

- **Cognitive Complexity too high** on `mousePressed`. The method nested an `if/else` (handle vs. no handle) inside another `if/else` (select-behind vs. normal lookup), with two `while` loops and a `for` loop underneath, plus a final tracker-swap block. It was around 80 lines.
- **Duplicated blocks** across the key/mouse handlers — the `getView() != null && getView().isEnabled()` guard appeared verbatim in seven methods.

My own reading flagged the same three things, which I list below.

## Code smells (before)

| # | Smell (Fowler) | Where | Symptom |
|---|----------------|-------|---------|
| 1 | Long Method | `mousePressed(MouseEvent)` | One method did handle lookup, select-behind logic, figure lookup, tracker selection and tracker swapping all inline. |
| 2 | Duplicated Code | `keyPressed`, `keyReleased`, `keyTyped`, `mouseClicked`, `mouseDragged`, `mouseReleased`, `mousePressed` | The exact guard `getView() != null && getView().isEnabled()` was copy-pasted into seven event methods. |
| 3 | Complicated / nested Conditional | the target-figure lookup inside `mousePressed` | A two-branch conditional (select-behind vs. normal) each holding its own loop-driven hit-test, mixing the *decision* with the two *searches*. |

There was also a smaller duplication: the tracker swap (deactivate old → assign → activate new → re-subscribe the listener) existed both in `mousePressed` and inside `TrackerHandler.toolDone`, with subtly different null handling.

## Plan

Work in small steps, each one behaviour-preserving and verified against the existing JUnit 4 / Mockito suite (`SelectionToolTest`) and the JGiven scenarios (`SelectionToolBDDTest`). The target shape is *Compose Method*: every method should read at one level of abstraction, so `mousePressed` becomes a short narrative of named steps and the detail moves into helpers. I split the work across two commits so each diff stayed reviewable.

## Refactorings applied

### Commit `8243c188` — simplify tracker logic

- **Extract Method** (Fowler) on the duplicated guard. The seven copies of `getView() != null && getView().isEnabled()` collapsed into one private `isViewActive()`, and each handler now reads `if (isViewActive())`. This is *Consolidate Duplicate Conditional Fragments* applied across methods, realised through Extract Method (smell #2).
- **Extract Method** on the body of `mousePressed`. I pulled the figure search into `findTargetFigure(view, drawingPoint, evt)` and the tracker decision into `resolveTracker(handle, figure, evt)`. `resolveTracker` keeps the original priority order: handle → `getHandleTracker`; selectable figure → `getDragTracker`; otherwise clear the selection (unless Shift is held) and return `getSelectAreaTracker`.
- **Compose Method** (Kerievsky) on `mousePressed` itself. After the extractions it reads as: guard, `super.mousePressed`, `findHandle`, `findTargetFigure`, `resolveTracker`, `setTracker`, delegate. Roughly 80 lines down to about 8 (smell #1).
- **Extract Method** to remove the swap duplication: I centralised the deactivate/assign/activate/re-subscribe sequence in `setTracker(Tool)` and gave it a single null guard at the top (return early if `newTracker == null`). `TrackerHandler.toolDone` now just calls `setTracker(getSelectAreaTracker())` instead of repeating the swap.

### Commit `35f97d94` — decompose the figure lookup

- **Decompose Conditional** (Fowler) on `findTargetFigure`. The condition became `isSelectBehindModifierHeld(evt)`, and the two branches became `findFigureBehindCurrentSelection(view, drawingPoint)` and `findFigureAtPoint(view, drawingPoint)`. `findTargetFigure` is now a single ternary that names the decision and the two outcomes (smell #3):

  ```java
  return isSelectBehindModifierHeld(evt)
          ? findFigureBehindCurrentSelection(view, drawingPoint)
          : findFigureAtPoint(view, drawingPoint);
  ```

- The condition predicate (`isSelectBehindEnabled()` AND ALT/CTRL held) moved into `isSelectBehindModifierHeld`, so the modifier-mask bit-twiddling is no longer inline in the search. The original search order and select-behind semantics are preserved — I kept the comment that the sequence must stay consistent with `DefaultHandleTracker`, `DefaultSelectAreaTracker` and `DelegationSelectionTool`.

## Strategy and verification

Because `SelectionTool` is itself a Strategy host (it swaps trackers as states), I was careful that none of these refactorings touched the *observable* state machine — only the internal control flow. Every step was kept green:

- After the Extract Method passes I re-ran `SelectionToolTest`. Its boundary section already covered the branches I was moving (null tracker, disabled view via `isViewActive`, and the handle/figure/empty arms of `resolveTracker`), so a regression in the extraction would have failed there immediately.
- The JGiven scenarios pinned the user-visible behaviour end to end: US1 (click selectable figure → `DragTracker`), US2 (click empty → selection cleared), US3 (click handle → `HandleTracker`), US4 (click empty → `SelectAreaTracker`), US5 (alt-click overlapping → figure behind). US5 in particular guards the `findFigureBehindCurrentSelection` path I split out in the second commit.
- CI (`.github/workflows/ci.yml`, `mvn clean install` on JDK 8) runs both suites on every PR with `enableAssertions=true`, so the invariant assertions (tracker never null, `selectBehindEnabled` defaults true, `supportsHandleInteraction` always true) also had to hold after each push.

The net result: `mousePressed` now reads as a short sequence of intent-named calls, the view guard exists once, and the figure hit-test is three focused helpers (`isSelectBehindModifierHeld`, `findFigureBehindCurrentSelection`, `findFigureAtPoint`) instead of one nested block. SonarLint no longer flags cognitive complexity or duplicated blocks on the class.
