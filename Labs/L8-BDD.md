# Lab 8 — Behaviour-Driven Development for the Selection Tool

I kept working on `org.jhotdraw.draw.tool.SelectionTool` (module `jhotdraw-core`). For this lab I turned the user stories I had already captured on the team backlog (tracked as GitHub issue #12) into executable Given-When-Then scenarios and automated them with JGiven, so the behaviour the user expects on the canvas is checked on every CI run.

## User stories mapped to scenarios

| User story | Given | When | Then |
|---|---|---|---|
| US1 — click a selectable figure to select it | a drawing with a selectable figure | the user clicks the figure | the `DragTracker` is activated |
| US2 — click empty area to deselect | an empty drawing area | the user presses on an empty area | the current selection is cleared |
| US3 — grab a figure's handle to manipulate it | a drawing with a handle at the click point | the user presses on the handle | the `HandleTracker` is activated |
| US4 — rubber-band select an area | an empty drawing area | the user presses on an empty area | the `SelectAreaTracker` is activated |
| US5 — alt-click overlapping figures to reach the one behind | overlapping figures with select-behind enabled | the user clicks with the Alt modifier | the `DragTracker` is activated on the figure behind |

US2 and US4 share the same Given/When (an empty-area press) but assert two different consequences of that one gesture: the previous selection is dropped, and the tool switches to area selection. I left them as separate scenarios because they map to separate stories and read more clearly that way.

Each row corresponds to one `@Test` in `jhotdraw-core/src/test/java/org/jhotdraw/draw/tool/bdd/SelectionToolBDDTest.java`, e.g. `clicking_on_a_selectable_figure_activates_drag_tracker()` reads:

```
given().a_drawing_with_a_selectable_figure();
when().the_user_clicks_on_the_figure();
then().the_DragTracker_is_activated();
```

## JGiven structure

The test class extends `ScenarioTest<GivenSelectionTool, WhenSelectionTool, ThenSelectionTool>`, which gives me the `given()`, `when()`, `then()` factory methods. Each is backed by a stage class extending `com.tngtech.jgiven.Stage`:

- **`GivenSelectionTool`** sets up the world. A `@BeforeStage` method (`setupMocks()`) builds the tool and the mocks; the step methods (`a_drawing_with_a_selectable_figure`, `a_drawing_with_a_handle_at_click_point`, `an_empty_drawing_area`, `a_disabled_drawing_view`, `overlapping_figures_with_select_behind_enabled`) stub the relevant `DrawingView`/`Drawing`/`Figure`/`Handle` behaviour, then `activate(mockEditor)`.
- **`WhenSelectionTool`** performs the gesture. It synthesises a `MouseEvent` and calls `tool.mousePressed(evt)`. `the_user_clicks_with_alt_modifier()` passes `InputEvent.ALT_DOWN_MASK` so `isSelectBehindModifierHeld(evt)` is true and the lookup goes through `findFigureBehindCurrentSelection` (US5).
- **`ThenSelectionTool`** asserts the outcome, e.g. `the_DragTracker_is_activated()`, `the_current_selection_is_cleared()`.

Every step method returns `self()`, which is what lets the `given().a_drawing_with_a_selectable_figure()` calls chain fluently and keeps each scenario reading like a sentence.

State flows between the three stages through scenario state, not method arguments. The Given fields (`tool`, `mockEditor`, `mockView`, `mockDrawing`, `mockFigure`, `mockHandle`, the dummy `JPanel`) are annotated `@ProvidedScenarioState`; the When and Then stages declare the same fields as `@ExpectedScenarioState` and JGiven injects the instances that the Given produced. The When stage also *provides* its own state — the `lastMouseEvent` and the two captured tracker instances (`firstTrackerInstance`, `secondTrackerInstance`, declared with `Resolution.NAME` so they're matched by field name rather than type, since both are `Tool`) — which the Then stage then consumes.

## Assertions and test level

The Then stage asserts with **AssertJ-core**, e.g. in `the_DragTracker_is_activated()`:

```java
Tool tracker = tool.callGetDragTracker(mockFigure);
assertThat(tracker).isNotNull();
assertThat(tracker).isInstanceOf(DragTracker.class);
```

For the side-effecting stories I verify against the Mockito mock instead — `the_current_selection_is_cleared()` does `verify(mockView).clearSelection()`, and the boundary scenario `no_tracker_action_is_performed()` does `verify(mockView, never()).findHandle(any(Point.class))` to confirm a disabled view short-circuits in `isViewActive()` before any tracker logic runs.

I deliberately test at the domain level: the scenarios drive a real `SelectionTool` (a `TestableSelectionTool` subclass declared inside `GivenSelectionTool` that exposes the protected `getDragTracker`/`getHandleTracker`/`getSelectAreaTracker`/`setTracker` methods) wired to Mockito mocks of `DrawingEditor`, `DrawingView`, `Drawing`, `Figure` and `Handle`. I never start the live Swing GUI. This keeps the scenarios fast and deterministic — no event-dispatch thread, no real frame to pump — and it's exactly why I removed the unused `assertj-swing-junit` dependency from `jhotdraw-core/pom.xml`; I'm asserting on the tool's behaviour and its collaborator interactions, not pixels.

## Extra scenarios beyond the stories

Alongside the five story scenarios I added a few that protect non-functional expectations:

- **Property change** — `setting_selectBehindEnabled_fires_property_change()` flips `selectBehindEnabled` and `then().selectBehindEnabled_is(false)` confirms the bound property updates. (The `the_property_change_event_is_fired()` step backed by `verify(mockListener)...` is in the Then stage for asserting the `PropertyChange` directly.)
- **Lazy-init invariant** — `select_area_tracker_lazy_init_returns_same_instance()`, plus the drag- and handle-tracker variants, request a tracker twice and assert `firstTrackerInstance` `isSameAs` `secondTrackerInstance`, confirming the `getXxxTracker()` lazy initialisers cache rather than recreate.
- **Handle-interaction invariant** — `selection_tool_always_supports_handle_interaction()` asserts `supportsHandleInteraction()` stays `true`.

All of this runs under the existing CI job (`.github/workflows/ci.yml`, `mvn clean install -s .maven-settings.xml` on JDK 8), so the JGiven scenarios execute as ordinary JUnit tests on every pull request next to the unit suite in `SelectionToolTest.java`.
