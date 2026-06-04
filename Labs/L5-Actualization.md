# Lab 5 — Actualization

For this lab I went back over the Selection Tool work and checked it against the SOLID principles and Clean Architecture. My feature is `org.jhotdraw.draw.tool.SelectionTool` in `jhotdraw-core`, and the two refactoring commits I refer to are `8243c188` (simplify tracker logic) and `35f97d94` (decompose the target-figure lookup).

## Part 1 — SOLID in the Selection Tool

### Single Responsibility Principle

The clearest example is the tracker split. `SelectionTool` is in one of three interaction modes — area selection, figure dragging, handle manipulation — and each mode is a separate class: `DefaultSelectAreaTracker`, `DefaultDragTracker`, `DefaultHandleTracker`. None of them knows about the other two. `SelectionTool` itself only decides *which* tracker is current and forwards events to it.

My own refactors pushed SRP down to the method level. Before `35f97d94`, `findTargetFigure()` did three jobs in one body: check the modifier keys, walk behind the current selection, and find the topmost figure at the point. I split it into `isSelectBehindModifierHeld()` (lines 253-257), `findFigureBehindCurrentSelection()` (267-280) and `findFigureAtPoint()` (294-308). Each now does one thing, and `findTargetFigure()` (240-244) is a two-line router. The same idea drove `8243c188`: `resolveTracker()` only picks a tracker, `setTracker()` (332-343) only swaps one in and out, and `isViewActive()` (409-411) only answers whether the view is usable.

### Open/Closed Principle

The Strategy design lets me add new selection behaviour without touching `SelectionTool`. The three tracker roles are interfaces (`HandleTracker`, `DragTracker`, `SelectAreaTracker`), and the public setters `setHandleTracker()`, `setDragTracker()`, `setSelectAreaTracker()` (lines 384-402) let a caller inject a different implementation. `DelegationSelectionTool` in the SVG sample is the existing proof of this — it reuses `SelectionTool` and supplies its own behaviour rather than editing the base class. So the class is open for extension (new tracker impls) but closed for modification.

### Liskov Substitution Principle

Two substitutions hold here. First, the `Default*` trackers stand in wherever their interface is expected — `getDragTracker()` returns a `DragTracker`, and the field it is stored in (`tracker`) is typed as `Tool`, so the concrete type never leaks. Second, `SelectionTool extends AbstractTool` and is used everywhere a `Tool` is expected (the editor holds it as the `Tool` interface). My overrides keep the contract: `mousePressed`, `keyPressed`, etc. all behave as a `Tool` is expected to, just guarded by `isViewActive()` first. The unit tests lean on this — `TestableSelectionTool` is a subclass that I hand to code expecting a `SelectionTool`, and nothing downstream notices the difference.

### Interface Segregation Principle

The design uses several small interfaces instead of one fat `Tool` that does everything. `Tool` is the event-handling contract; `HandleTracker`, `DragTracker` and `SelectAreaTracker` each add only the one method their role needs (`setHandles`, `setDraggedFigure`, and nothing extra for the area tracker). `Handle` is its own focused interface for a draggable control point. Because the tracker interfaces are separate, a class that only drags figures never has to implement handle logic it does not use.

### Dependency Inversion Principle

`SelectionTool` depends on abstractions, not concretes, almost everywhere. The current tracker is held as `Tool`; collaborators come in as `DrawingView`, `Drawing`, `Figure`, `Handle` interfaces; the trackers it returns are typed by their interfaces. The only place it names a concrete class is the lazy default creation inside `getHandleTracker`/`getDragTracker`/`getSelectAreaTracker`, and the setters exist precisely so that default can be replaced. This is exactly what my tests exploit: in `SelectionToolTest` I inject Mockito mocks of `DrawingView`, `Drawing`, `Figure` and `Handle`, and BDD scenarios inject mock trackers through the setters, so I can verify routing without a real Swing canvas.

| Principle | Where it shows up in the Selection Tool |
|-----------|------------------------------------------|
| SRP | One tracker per interaction mode; each private method (`isSelectBehindModifierHeld`, `findFigureAtPoint`, `setTracker`, `isViewActive`) does one job |
| OCP | Strategy trackers + `setHandleTracker`/`setDragTracker`/`setSelectAreaTracker` add behaviour without editing `SelectionTool`; `DelegationSelectionTool` proves it |
| LSP | `Default*` trackers substitute for their interfaces; `SelectionTool`/`TestableSelectionTool` substitute for `AbstractTool`/`Tool` |
| ISP | Small focused interfaces: `Tool`, `HandleTracker`, `DragTracker`, `SelectAreaTracker`, `Handle` |
| DIP | Depends on `Tool`/`*Tracker`/`DrawingView` abstractions; setters allow injecting mocks, which the tests use |

## Part 2 — Clean Architecture in this case study

The Maven module layout maps cleanly onto the Clean Architecture rings.

- **Inner ring (domain / abstractions and logic):** `jhotdraw-api` and `jhotdraw-core`. This is where `Tool`, `DrawingView`, `Drawing`, `Figure`, `Handle`, the tracker interfaces, and `SelectionTool` itself live. These define *what* selection means with no knowledge of how the pixels get on screen.
- **Outer ring (frameworks, UI, wiring):** `jhotdraw-gui`, `jhotdraw-app`, and `jhotdraw-samples` (e.g. the runnable SVG demo `org.jhotdraw.samples.svg.Main`). This is where Swing components, application menus and the concrete editor wiring sit.

Dependencies point inward. The UI and sample modules depend on the core abstractions; the core does not depend on them. Swing is at the very outer edge — `SelectionTool` consumes `java.awt.event.MouseEvent`/`KeyEvent`, but it never reaches up into a `JPanel` or an application window. It talks to the framework only through the `DrawingView`/`Drawing` interfaces defined in the inner ring, which is the dependency-inversion boundary Clean Architecture asks for.

This is the link to actualization. Every change I made — extracting `findTargetFigure`/`resolveTracker`, decomposing the figure lookup, centralising `setTracker` — stayed entirely inside `jhotdraw-core`. I never had to open a GUI or app module, and nothing in `jhotdraw-gui`/`jhotdraw-app` needed editing to keep the build green. The dependency direction is what makes that possible: because the UI depends on the core and not the other way round, I could refactor and re-test the core tool logic in isolation, with mocks standing in for the outer ring, and the CI build (`mvn clean install` on JDK 8) confirmed the outer modules still compiled against the unchanged interfaces.
