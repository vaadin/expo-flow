# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **Vaadin 25.2 full-stack demo application** showcasing Java Flow views, Hilla React views, Spring AI integration, Vaadin Signals, and runtime theming with Aura. It demonstrates a hybrid architecture where both server-side Java components and client-side React components coexist.

Stack: Java 25, Kotlin 2.3 (version from the Spring Boot parent), Vaadin 25.2.6, Spring Boot 4.1.0, Spring AI 2.0.0, draiv, H2.

Java is 25 (not 21) because the draiv libraries are compiled for Java 25.

## Commands

```bash
mvn                            # Run in development mode (default goal: spring-boot:run)
mvn test                       # Run the browserless tests
mvn clean package -Pproduction # Production build (includes the frontend bundle)
```

The app runs on port **8888** (`server.port=${PORT:8888}`).

`OPENAI_API_KEY` must be set — `application.properties` references it without a
default, so the application context fails to start without it, not just the Chat
view.

The Devoxx talk search needs no key of its own: it uses the provider selected by
`PRESENTER_CHAT_PROVIDER` (default `openai`, model `OPENAI_TEXT_MODEL`, default `gpt-5.5`).
Set `DEVOXX_DEMO_TIME=2026-10-07T10:15` to rehearse "what's next?" outside conference hours.

The H2 database is a **file** at `~/t-shirt-orders`, so only one instance can run
at a time. A second one fails with "Database may be already in use". To run a
throwaway instance alongside a running app, override both the datasource and the
port. The H2 console is at `/h2`.

There is also an `it` profile in the pom, but no `*IT` tests exist, so
`mvn -Pit integration-test` currently runs nothing.

## Branches

`devoxxBEL26` is the Devoxx Belgium 2026 branch. `v25` is behind it in every file (Vaadin
25.1, the old Slider API, Lumo utility classes) and was merged in with `-s ours`, so the
history contains it while the code stays this branch's. When syncing with `v25` again,
resolve toward `devoxxBEL26`.

## Architecture

### Hybrid View Model

Two types of views coexist under the same router:

- **Java Flow views** (`src/main/java/.../views/`): Server-rendered components with `@Route` and `@Menu` annotations. The layout, navigation, and state live on the server.
- **Kotlin Flow views** (`src/main/kotlin/.../views/`): Same Flow programming model, written in Kotlin. `KotlinPlaygroundView.kt` is the Kotlin twin of `PlaygroundView.java`. `kotlin-maven-plugin` is bound to `process-sources`/`process-test-sources` so Kotlin compiles before javac and both languages land in the same `target/classes`; route scanning and `vaadin.allowed-packages` treat them identically. Note `@Menu(order = ...)` is a `double`, so Kotlin needs `8.0`, not `8`.
- **React/Hilla views** (`src/main/frontend/views/`): Client-rendered TSX files. The file-router auto-registers them based on file path.

`MainLayout.java` is the shared shell (AppLayout + SideNav) used by all Java views. React views render into the Hilla outlet.

### Theming (Aura)

The app uses **Aura**, the Vaadin 25 default theme. It is loaded from
`Application.java`, *not* through a `@Theme` folder:

```java
@StyleSheet(Aura.STYLESHEET)
@StyleSheet("styles.css")
```

- All CSS lives in `src/main/resources/META-INF/resources/`. `styles.css` is the master stylesheet and `@import`s one file per view; give a new view its own `<view>-view.css` and import it there.
- **Aura and Lumo are mutually exclusive.** Style with the base style `--vaadin-*` properties, Aura's `--aura-*` properties and your own classes. No Lumo stylesheet is loaded, so `--lumo-*` properties and `LumoUtility` classes are undefined here and silently do nothing.
- Dark mode is the CSS `color-scheme` property, not a `theme="dark"` attribute.
- `themes/*-theme.css` are alternative themes, swapped at runtime by the ComboBox in `MainLayout.createFooter()` via `Page::addStyleSheet`. To add one, drop in a `<name>-theme.css` and add its display name to that ComboBox.
- `color-cycle.js` ("unicorn mode", the checkbox in `MainLayout.createFooter()`) animates the Aura accent and background colours through the hue spectrum.

### Client–Server Communication (Hilla)

Java service classes annotated with `@BrowserCallable` + `@AnonymousAllowed` are automatically exposed as type-safe TypeScript endpoints. The Hilla codegen runs during `mvn` and outputs to `src/main/frontend/generated/`:
- `endpoints.ts` — callable TypeScript functions for each Java method
- `routes.tsx` — auto-generated React routes
- Type models for all Java DTOs

**Never manually edit files in `src/main/frontend/generated/`, `vite.generated.ts`, or `types.d.ts`** — they are overwritten on every build. In particular, `types.d.ts` only declares `'*.css?inline'`, so a plain `import './foo.css'` in a `.tsx` will not type-check; put such CSS in the app stylesheet instead.

### Signals / Reactive State

`ShoppingListView.java` demonstrates **Vaadin Signals** (`ListSignal`, `Signal.computed`, `bindChildren()`, `bindValue()`, `bindText()`). The `@vaadin/hilla-react-signals` package is the React counterpart. This is a key differentiator of Vaadin 25 — signals provide reactive state sync between client and server.

### React Components in Java Views

`SwitchComponent.java` wraps a MUI `<Switch>` through `ReactAdapterComponent`:
`@NpmPackage` + `@JsModule` point at `src/main/frontend/components/react-switch.tsx`,
and state crosses the boundary via `getState`/`setState`/`addStateChangeListener`.
`ReactAdapterElement` renders into **light DOM**, so global stylesheets reach it.
`ExternalComponentView` is the demo.

### Data Layer

- Spring Data JPA repositories with H2 (file-based at `~/t-shirt-orders`)
- `DataGenerator.java` populates demo data on startup via `CommandLineRunner`
- Base entity: `AbstractEntity.java` (UUID primary key). Note `TShirtOrder` does not extend it — it has its own `Long` id.
- Validation: Jakarta annotations on entities + `BeanValidationBinder` in forms

### AI Integration

`ChatView.java` uses Spring AI (`ChatModel`) wrapped in `SpringAILLMProvider` and
driven by `AIOrchestrator`, which wires the `MessageList` and `MessageInput`
together. It extends `UploadDropZone` and uses the modular upload components
(`UploadManager`, `UploadButton`, `UploadFileList`) for attachments — images,
PDFs and text, max 5 files at 5 MB each.

### Devoxx talk search (draiv)

`views/devoxx/DevoxxTalksView` searches the Devoxx Belgium 2026 talks in plain
language, modelled on draiv's `vaadin-grid-search-ui` example. The AI does **not**
translate the text into a query: `AiSearchBar` hands a goal (`DevoxxTalksView.AI_GOAL`)
to draiv's `TextPresenterService`, and the LLM operates the ordinary `TalkSearchForm`
on screen through Spring AI tool calls (`inspect`, `apply`, `apply_many`) — it fills in
the fields and clicks Search. Changed fields flash (`ChangeIndication`).

- The only draiv wiring is the `chatToolCatalog` bean in `ai/AiSearchConfiguration`;
  the rest is auto-configured. draiv registers a `@Primary ChatModel` for the selected
  provider, which `ChatView` gets too.
- Form fields have stable ids (`filter-*`, `search-button`, `reset-button`); the goal
  refers to them. Keep ids and goal in sync.
- Each `assist()` starts without memory, so the view passes the previous request into the
  goal — that is what makes "only for beginners" refine instead of reset.
- Speed is mostly the number of model rounds, so the goal spells out the tool operations and
  asks for one `apply_many`: that took a question from 4–9 tool calls to 2–3. Measured with
  that goal, `gpt-5.5` got all test questions right at about 4 s each; `gpt-4o-mini` and
  `gpt-5.4-mini` were faster but unreliable (e.g. ignoring the time window for "what's next").
- Goal wording that matters: "what's next" is a 60-minute window, because talk slots often
  start 40–70 minutes apart and 30 minutes regularly found nothing. When a track covers the
  topic ("security"), the AI selects the track instead of typing Topics, which matched talks
  that only mention the word.
- The grid shows When, Title and Track; speakers, format and room sit in the row details
  (a `LitRenderer`; `.talk-facts` in `devoxx-view.css` gives them equal columns).
- Row details follow the selection. Arrow keys browse the talks through the `executeJs`
  keydown listener on the grid: it sets the grid's client-side `activeItem`, exactly what a
  click does. Selecting from a server-side focus listener instead makes a mouse click open and
  immediately close the details: the server's select sets `activeItem`, and the click that
  follows toggles it off. The listener also re-sends the arrow key when the focus lands on a
  details cell, since vaadin-grid makes open details a keyboard stop of their own.
- `devoxx/DevoxxTalkService` loads the five `/api/public/schedules/{day}` from
  `devoxx.schedule-url` on startup and falls back to the copies in
  `src/main/resources/devoxx/`. Filtering is in memory (`TalkFilter`), no JPA.
- draiv is only published as a SNAPSHOT; `draiv.version` in the pom pins one timestamped
  build, also for its transitive modules in `dependencyManagement`.
- draiv also brings Mistral, Anthropic, an MCP server and realtime voice.
  `application.properties` gives the unused providers placeholder keys and disables the
  MCP server and voice (the MCP server would let anyone on the network drive the UI).

## Key Configuration

- **Port**: `server.port=${PORT:8888}` in `application.properties`
- **Feature flags**: `src/main/resources/vaadin-featureflags.properties`. Only `aiComponents` is needed on 25.2 — Slider, Badge, modular upload and message list attachments are all GA. Unknown flag names log an "Unsupported feature flag" warning on every startup rather than failing.
- **Prettier**: single quotes, 120-char line width (`.prettierrc.js`)
- **pnpm**: `shamefully-hoist=true` (flat node_modules)
- **TypeScript paths**: `Frontend/*` maps to `src/main/frontend/*`
- **Vaadin allowed packages**: whitelist in `application.properties` controls which Java packages Hilla can expose

## Testing

Tests use `SpringBrowserlessTest` from the `browserless-test-spring` artifact. It
drives views **in-process — there is no browser and no running server**, so tests
are fast and need no Playwright setup.

`PlaygroundViewTest.java` shows the pattern: **extend** `SpringBrowserlessTest`
(do not inject it), annotate with `@SpringBootTest`, then:

```java
navigate(PlaygroundView.class);
var button = $(Button.class).withText("Say hello").single();
test(button).click();
```

`src/test/resources/application.properties` overrides the datasource with an
in-memory H2 and supplies a dummy OpenAI key, so tests do not touch
`~/t-shirt-orders` or need real credentials. It replaces the main file rather than
adding to it, so it repeats the draiv properties, and it blanks `devoxx.schedule-url`
so the Devoxx tests run on the bundled schedule.

`DevoxxTalksDrivingTest` drives the talk search form through draiv's `inspect`/`apply`
tools exactly as the LLM does, without a model — the cheap way to check the AI can
still operate the form after changing it.

Navigating to a view in such a test is also the cheapest way to check that it
still constructs — useful after dependency bumps, since experimental-component
and feature-flag breakage only shows up at runtime.
