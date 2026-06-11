# Concept Location: The Selection Tool

To find the classes that implement the Selection feature I located the concept dynamically rather than by reading code. I launched the runnable SVG sample (`org.jhotdraw.samples.svg.Main` in `jhotdraw-samples-misc`), set a breakpoint on the first line of `SelectionTool.mousePressed(MouseEvent)` (`SelectionTool.java:217`), and then exercised the feature three ways on the canvas: clicking a figure, clicking one of a figure's handles, and clicking empty space. Each click suspended the debugger at the same entry point, which confirmed that `SelectionTool` is the controller Tool the editor delegates mouse events to and the natural starting point of the concept.

From that breakpoint I stepped into the body. `view.findHandle(anchor)` took me into `DrawingView`; when it returned null I followed `findTargetFigure(...)` into `Figure.isSelectable()`/`contains()` and `Drawing.findFigureBehind(...)`; then `resolveTracker(handle, figure, evt)` (`SelectionTool.java:318`) picked a tracker and `setTracker(...)` activated it. Stepping into `tracker.mousePressed(evt)` dropped me into one of the three Default trackers depending on which gesture I had performed. Holding Alt while clicking overlapping figures took the `isSelectBehindModifierHeld` branch and stepped into `findFigureBehindCurrentSelection`. Walking those call chains gave me the set of collaborating classes below.

| Domain Class | Responsibility |
| --- | --- |
| `SelectionTool` | Controller/entry Tool the debugger lands in first; holds the current tracker and routes mouse/key events to it (Strategy context). |
| `AbstractTool` | Superclass providing common Tool plumbing: the `anchor` point, view/editor accessors, and the `fire*` event helpers. |
| `Tool` | Interface defining the Tool contract (`activate`, `mousePressed`, `draw`, listener methods) that `SelectionTool` and the trackers implement. |
| `HandleTracker` / `DefaultHandleTracker` | Tracker state for manipulating a handle; default impl drags the handle and its compatible handles. |
| `DragTracker` / `DefaultDragTracker` | Tracker state for dragging a selected figure; default impl moves the figure with the mouse. |
| `SelectAreaTracker` / `DefaultSelectAreaTracker` | Tracker state for rubber-band area selection; default impl draws the selection rectangle and selects enclosed figures. |
| `DrawingView` | The view clicked on; resolves handles and figures (`findHandle`, `findFigure`), converts coordinates (`viewToDrawing`), and manages selection state. |
| `Drawing` | The figure container; `findFigureBehind(...)` walks the z-order for the select-behind gesture. |
| `DrawingEditor` | Editor context passed to `activate`/`deactivate` when trackers are swapped. |
| `Figure` | The drawable object being selected; queried via `isSelectable()` and `contains(point)`. |
| `Handle` | A draggable control point on a selected figure; the target a `HandleTracker` operates on. |
| `ToolEvent` / `ToolAdapter` | Event plumbing; the inner `TrackerHandler` extends `ToolAdapter` to catch tracker `toolDone` events and reset to the select-area tracker. |

The initial concept set is therefore `SelectionTool` together with `AbstractTool`/`Tool`, the three tracker pairs (`HandleTracker`/`DefaultHandleTracker`, `DragTracker`/`DefaultDragTracker`, `SelectAreaTracker`/`DefaultSelectAreaTracker`), and the collaborators `DrawingView`, `Drawing`, `DrawingEditor`, `Figure`, `Handle`, and the `ToolEvent`/`ToolAdapter` event types.
