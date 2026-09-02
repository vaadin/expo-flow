# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **Vaadin 25.2 full-stack demo application** showcasing Java Flow views, Hilla React views, Spring AI integration, Vaadin Signals, and runtime theming with Aura. It demonstrates a hybrid architecture where both server-side Java components and client-side React components coexist.

Stack: Java 21, Kotlin 2.3 (version from the Spring Boot parent), Vaadin 25.2.6, Spring Boot 4.1.0, Spring AI 2.0.0, H2.

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

The H2 database is a **file** at `~/t-shirt-orders`, so only one instance can run
at a time. A second one fails with "Database may be already in use". To run a
throwaway instance alongside a running app, override both the datasource and the
port. The H2 console is at `/h2`.

There is also an `it` profile in the pom, but no `*IT` tests exist, so
`mvn -Pit integration-test` currently runs nothing.

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

- All CSS lives in `src/main/resources/META-INF/resources/`. `styles.css` is the master stylesheet and `@import`s the per-view files (`main-layout.css`, `components-view.css`, `quiz-view.css`, `react-switch.css`).
- **Aura and Lumo are mutually exclusive.** Do not introduce `--lumo-*` custom properties — they are undefined here. Use the base style `--vaadin-*` properties and Aura's `--aura-*` properties instead.
- Dark mode is the CSS `color-scheme` property, not a `theme="dark"` attribute.
- `themes/*-theme.css` are alternative themes, swapped at runtime by the ComboBox in `MainLayout.createFooter()` via `Page::addStyleSheet`. To add one, drop in a `<name>-theme.css` and add its display name to that ComboBox.
- `color-cycle.js` ("unicorn mode") animates the Aura accent and background colours through the hue spectrum.

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
`~/t-shirt-orders` or need real credentials.

Navigating to a view in such a test is also the cheapest way to check that it
still constructs — useful after dependency bumps, since experimental-component
and feature-flag breakage only shows up at runtime.
