# Lab 7 — Testing the Selection Tool

This lab covers how I verified `org.jhotdraw.draw.tool.SelectionTool` with unit tests. After the two refactors on `altan/develop` (8243c188 and 35f97d94) I had small, named methods to target, so I wrote a JUnit 4 + Mockito suite that exercises them without ever opening a Swing window.

## Test setup in the pom

I added the test dependencies to `jhotdraw-core/pom.xml`. For this lab the ones that matter are JUnit `4.13.2` and `mockito-core` `3.12.4` (the JGiven/AssertJ entries are for the BDD suite in Lab 8). I also reconfigured surefire `3.2.5`:

```xml
<configuration>
    <enableAssertions>true</enableAssertions>
    <argLine>
        --add-opens java.base/java.lang=ALL-UNNAMED
        --add-opens java.base/java.lang.reflect=ALL-UNNAMED
    </argLine>
</configuration>
```

The `--add-opens` lines are needed because Mockito uses reflection to build its mocks. `enableAssertions=true` is the important one for the invariant section below — by default the JVM runs with `assert` disabled, so without this flag those checks would silently pass and test nothing.

## Mocking the collaborators

`SelectionTool` talks to `DrawingEditor`, `DrawingView`, `Drawing`, `Figure` and `Handle`. All of these are interfaces in the framework, and in production they are backed by live Swing components. Driving a real canvas in a unit test would be slow and flaky, so in `setUp()` I mock all five with `Mockito.mock(...)` and wire just enough behaviour to get the tool running:

- `mockEditor.getActiveView()` / `findView(...)` return `mockView`
- `mockView.isEnabled()` returns `true`, `getDrawing()` returns `mockDrawing`
- `viewToDrawing(Point)` returns a fixed `Point2D.Double(100, 100)`
- `findHandle(...)` and `findFigure(...)` return `null` by default, so each test only stubs the lookup it cares about

This lets me assert on *which tracker the tool picks* purely at the domain level — no `JFrame`, no event-dispatch thread.

To reach the `protected` factory and swap methods I added a small `TestableSelectionTool` subclass that exposes `getSelectAreaTracker()`, `getDragTracker(Figure)`, `getHandleTracker(Handle)` and `setTracker(Tool)`.

## The three sections of `SelectionToolTest`

I split the ~20 tests into three labelled groups.

| Section | What it checks | Example tests |
|---|---|---|
| A. Best-case | Normal behaviour with valid inputs: property accessors, property-change firing, lazy creation of trackers, tracker activate/deactivate delegation, tracker swapping | `testDefaultSelectBehindEnabled`, `testGetSelectAreaTrackerLazyInit`, `testSetTrackerSwapsTrackers` |
| B. Boundary | Edge inputs and each branch of `resolveTracker`: null tracker, disabled view, the handle / selectable-figure / empty-area branches, a no-op property set | `testSetTrackerWithNull`, `testMousePressedWithDisabledView`, `testResolveTrackerWith*`, `testSetSelectBehindEnabledNoChangeDoesNotFire` |
| C. Invariant | Properties that must always hold on any instance, asserted with the Java `assert` keyword | `testInvariantTrackerNeverNull`, `testInvariantSelectBehindEnabledDefaultTrue`, `testInvariantSupportsHandleInteractionAlwaysTrue` |

### A — best case

These confirm the tool behaves under normal use. `testSelectBehindEnabledFiresPropertyChange` registers a mock `PropertyChangeListener`, flips `selectBehindEnabled` to `false`, and verifies the listener fired. The lazy-init tests call a factory twice and use `assertSame(first, second)` to prove the `Default*` tracker is created once and cached. `testSetTrackerSwapsTrackers` injects two mock `Tool`s in sequence and verifies the old one got `deactivate(editor)` and the new one `activate(editor)` — the centralised swap logic from 8243c188.

### B — boundary and branch coverage

The core of `mousePressed` is `resolveTracker(handle, figure, evt)`, which has three exits. I cover each one by stubbing the view's lookups and then asserting the matching tracker type is created:

- **Handle branch** — `findHandle(...)` returns `mockHandle`, expect a `HandleTracker` (`testResolveTrackerWithHandleReturnsHandleTracker`).
- **Selectable figure branch** — `findHandle` returns `null`, `findFigure` returns a `mockFigure` with `isSelectable()` true, expect a `DragTracker`.
- **Empty area branch** — both lookups return `null`, expect a `SelectAreaTracker`.

The edge cases:

- `testSetTrackerWithNull` passes `null` into `setTracker`; the null guard added in 8243c188 means it is a no-op, no `NullPointerException`, and the tool still deactivates cleanly.
- `testMousePressedWithDisabledView` stubs `isEnabled()` to `false`, then verifies the injected tracker's `mousePressed` is **never** called. This pins down the `isViewActive()` guard that the same commit pulled out of seven event handlers.
- `testSetSelectBehindEnabledNoChangeDoesNotFire` sets the property to its current value and uses `verify(listener, never())` — `PropertyChangeSupport` skips the event when old equals new, and I wanted that documented as expected behaviour rather than a bug.

When I first wrote the disabled-view test it failed because my `setUp()` default already stubbed `isEnabled()` to `true`; I had to re-stub it to `false` inside the test before `activate()`. A breakpoint in `isViewActive()` confirmed the guard was the thing short-circuiting `mousePressed`.

### C — invariants with the `assert` keyword

Three things should hold for any `SelectionTool` regardless of how it was constructed: the select-area tracker is never null, `selectBehindEnabled` defaults to `true`, and `supportsHandleInteraction()` is always `true`. I express these with the Java `assert` keyword on a freshly constructed instance, e.g.:

```java
assert freshTool.isSelectBehindEnabled() : "selectBehindEnabled must be true by default";
```

This is why `enableAssertions=true` in surefire matters — I checked that it actually runs by temporarily breaking one invariant and watching the test go red; with assertions disabled it had been passing regardless. I kept a parallel `assertTrue(...)` alongside each `assert` so the test still fails loudly even if someone runs it without `-ea`.

## How to run

```
mvn -pl jhotdraw-core test
```

The suite also runs automatically in CI: `.github/workflows/ci.yml` executes `mvn clean install -s .maven-settings.xml` on JDK 8 for every pull request, so both the unit tests and the BDD scenarios from the next lab gate every merge into `develop`.
