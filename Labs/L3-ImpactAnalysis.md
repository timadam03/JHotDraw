# Lab 3 — Impact Analysis: SelectionTool

## Starting point

Concept location (Lab 2) ended on `org.jhotdraw.draw.tool.SelectionTool` as the class that implements the Selection Tool feature. Following the static + dynamic impact analysis process in Figure 7.9 (Rajlich), I mark `SelectionTool` as **CHANGED**, mark all of its direct neighbours **NEXT**, and then walk the graph: each time I take a class off the NEXT set I decide whether a change to `SelectionTool` would actually force a change in it (**CHANGED**), whether it would only relay the change further (**PROPAGATES**), or whether it would be left **UNCHANGED**. New neighbours of a CHANGED/PROPAGATES class get added to NEXT. I stop when NEXT is empty.

I built the initial neighbour set from the imports and field/parameter types in `SelectionTool.java` plus the call sites I traced while reading `mousePressed`, `resolveTracker`, `findTargetFigure`, `findFigureBehindCurrentSelection`, `findFigureAtPoint` and `setTracker`. I sanity-checked the reverse direction with a `grep -rl "new SelectionTool"` / `extends SelectionTool` to see who depends *on* the tool — only `DelegationSelectionTool` and the editor wiring do, and neither is touched by the refactor.

## Walking the graph

Starting from `SelectionTool` (**CHANGED**), the first ring of NEXT nodes is: `AbstractTool`, `Tool`, the three tracker interfaces (`DragTracker`, `HandleTracker`, `SelectAreaTracker`) and their three default impls (`DefaultDragTracker`, `DefaultHandleTracker`, `DefaultSelectAreaTracker`), then `DrawingView`, `Drawing`, `DrawingEditor`, `Figure`, `Handle`, and the event plumbing `ToolEvent` / `ToolAdapter` / `ToolListener`.

**`AbstractTool` (superclass) — UNCHANGED.** `SelectionTool` calls `super.mousePressed`, `getView`, `getEditor`, `firePropertyChange`, `fireToolDone`, `fireAreaInvalidated`, `fireBoundsInvalidated` and reads `anchor`. All of these are existing protected members; the refactor (commits `8243c188` and `35f97d94`) only re-organised code *inside* `SelectionTool` and added private helpers. It never changed how the superclass is called, so nothing propagates upward.

**`Tool` interface — UNCHANGED.** The local field `tracker` is typed as `Tool` and `resolveTracker`/`setTracker` pass `Tool` around, but I never added a method to the interface — `setTracker` still uses `activate`, `deactivate`, `mousePressed`, `addToolListener`, `removeToolListener`, all pre-existing. Visited, left alone.

**`DragTracker`, `HandleTracker`, `SelectAreaTracker` interfaces — UNCHANGED.** These are the State roles in the Strategy pattern. `getDragTracker`, `getHandleTracker`, `getSelectAreaTracker` still call `setDraggedFigure`, `setHandles`, etc. The whole point of the refactor was to keep their contracts intact, so a change to `SelectionTool` does not force a change here. They are the boundary where the walk stops on the tracker side.

**`DefaultDragTracker`, `DefaultHandleTracker`, `DefaultSelectAreaTracker` — UNCHANGED.** Lazily instantiated by the `getXxxTracker` methods exactly as before. `DefaultHandleTracker` is mentioned in a Javadoc note about keeping the figure-search order consistent; I checked that order in `findFigureAtPoint` and it matches, so the note is honoured and the class does not need editing.

**`DrawingView` — UNCHANGED (interface), PROPAGATES at most.** This is the busiest collaborator: `findHandle`, `findFigure`, `viewToDrawing`, `getDrawing`, `getSelectedFigures`, `getCompatibleHandles`, `clearSelection`, `setHandleDetailLevel`, `isEnabled`. Every one of these is an existing method on the `DrawingView` interface. The refactor moved the calls into helper methods but kept the same calls with the same arguments, so the interface is visited and left UNCHANGED.

**`Drawing` — UNCHANGED.** Reached through `view.getDrawing()` inside `findFigureBehindCurrentSelection` and `findFigureAtPoint`, then `drawing.findFigureBehind(...)`. Pre-existing method, unchanged signature. Visited, left alone.

**`DrawingEditor` — UNCHANGED.** Only used as the activate/deactivate context (`activate(DrawingEditor)`, `deactivate(DrawingEditor)`, `getEditor()`). No change.

**`Figure` — UNCHANGED.** The tool reads `isSelectable()` and `contains(Point2D.Double)`. Both already existed; the select-behind loop and the "prefer current selection" loop use them as before. Visited, left alone.

**`Handle` — UNCHANGED.** Returned by `view.findHandle` and passed into `getHandleTracker`. The tool only holds and forwards it; no member of `Handle` is touched.

**`ToolEvent`, `ToolAdapter`, `ToolListener` — UNCHANGED.** The inner class `TrackerHandler extends ToolAdapter` overrides `toolDone`, `areaInvalidated`, `boundsInvalidated` and reads `ToolEvent.getInvalidatedArea()`. These overrides and that accessor are unchanged by the refactor, and `ToolListener` is only used through `addToolListener`/`removeToolListener`. Event plumbing is visited and left alone.

After processing all of the above, NEXT is empty and the walk terminates.

## Conclusion of the walk

The change is contained inside the **single CHANGED class, `SelectionTool`**. Every neighbour I visited was either a collaborator interface (`Tool`, the three tracker interfaces, `DrawingView`, `Drawing`, `DrawingEditor`, `Figure`, `Handle`) or a class whose contract the refactor deliberately preserved (`AbstractTool`, the three `Default*` trackers, the three event classes). None of them had to change, because both refactor commits only restructured `SelectionTool`'s own body — extracting `findTargetFigure`/`resolveTracker`/`isViewActive`/`setTracker` (`8243c188`) and then splitting `findTargetFigure` into `isSelectBehindModifierHeld`/`findFigureBehindCurrentSelection`/`findFigureAtPoint` (`35f97d94`) — while keeping every external call unchanged.

## Table 1 — Packages visited during impact analysis

| Package name | # of classes | Comments |
|---|---|---|
| `org.jhotdraw.draw.tool` | 20 | Home package of the feature; holds `SelectionTool` (CHANGED) plus `Tool`/`AbstractTool` and the three tracker interfaces + three `Default*` impls (all visited, UNCHANGED). |
| `org.jhotdraw.draw` | 23 | Provides the core collaborators `DrawingView`, `Drawing`, `DrawingEditor` that the tool queries and mutates; visited, UNCHANGED. |
| `org.jhotdraw.draw.figure` | 27 | Supplies `Figure` (`isSelectable`, `contains`), the objects the tool selects and drags; visited, UNCHANGED. |
| `org.jhotdraw.draw.handle` | 26 | Supplies `Handle`, the draggable control point returned by `findHandle` and fed to the handle tracker; visited, UNCHANGED. |
| `org.jhotdraw.draw.event` | 26 | Event plumbing — `ToolEvent`, `ToolAdapter`, `ToolListener` used by the inner `TrackerHandler` to relay tool events; visited, UNCHANGED. |

## Estimated impact set

**{ `SelectionTool` }** — the only CHANGED class. All other classes in the five packages above were visited during the walk and marked UNCHANGED, because the refactor preserved the contracts of the interfaces (`Tool`, `DragTracker`, `HandleTracker`, `SelectAreaTracker`, `DrawingView`, `Drawing`, `DrawingEditor`, `Figure`, `Handle`) and of the collaborating classes (`AbstractTool`, the `Default*` trackers, the event classes). The test code (`SelectionToolTest`, the JGiven BDD stages) is also affected as a consumer, but it sits in `src/test` and follows the change rather than being part of the production impact set.
