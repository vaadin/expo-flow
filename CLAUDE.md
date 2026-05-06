# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Vaadin 25 playground application for JAX 2026, demonstrating both Java-based Flow views and React-based Hilla client-side views. Single-module Maven project with Spring Boot 4.0.6, Java 25, Vaadin 25.1.5, and H2 database.

## Build & Run Commands

```bash
mvn                        # Default goal: spring-boot:run (starts dev server on port 8888)
mvn clean package          # Production build (includes frontend compilation via vaadin-maven-plugin)
mvn verify -Pit            # Run integration tests (TestBench, activated via 'it' profile)
```

The Maven wrapper (`./mvnw`) is available for reproducible builds.

## Architecture

**Hybrid stack:** Java Vaadin Flow views and React/Hilla client-side views coexist in the same application.

- `src/main/java/com/example/application/` — Java backend
  - `views/` — Vaadin Flow views using `@Route` and `@Menu` annotations
  - `views/MainLayout.java` — App shell with `@Layout`, uses `AppLayout` + `SideNav`, dynamic theme switcher (7 themes), dark mode toggle, and "Unicorn mode"
  - `components/` — `SwitchComponent` Java adapter for the React MUI Switch
  - `service/` — Spring services; all are `@BrowserCallable` and `@AnonymousAllowed`
  - `data/entity/` — JPA entities (`Person`, `TShirtOrder`, `Talk`, `AbstractEntity`)
  - `data/repository/` — Spring Data JPA repositories (Person, TShirtOrder, Talk)
  - `data/generator/` — `DataGenerator` (100 random persons on startup), `JAXDataInitializer` (JAX talk data)
- `src/main/frontend/` — Frontend code
  - `views/` — React views using Hilla file-based routing (`hello-react-world.tsx`)
  - `components/` — React components (`react-switch.tsx` + CSS, `color-cycle.js`)
- `src/main/resources/META-INF/resources/` — Static resources
  - `themes/` — 7 theme CSS files (default, carbon, less-carbon, linear, sparkasse, dhl, deutsche-bank)
  - `styles.css`, `main-layout.css`, `components-view.css` — Global and view-specific styles
- `Application.java` — Entry point; uses Aura theme (`@StyleSheet(Aura.STYLESHEET)`), `@Push` for WebSocket support, `@EnableScheduling`

**Routing:** Views use `@Route` with `@Menu(order=N)` for automatic navigation menu generation via `MenuConfiguration`. The `ComponentsView` is aliased to `/` as the default route.

**Data layer:** H2 file-based database (`~/t-shirt-orders`), Spring Data JPA with auto DDL, Jakarta Bean Validation.

**AI integration:** `ChatView` uses Spring AI with OpenAI (`OPENAI_API_KEY` env var required, model `gpt-4o-mini`). `AiFilterView` uses `@Tool` annotations for AI-driven JAX talk filtering.

## Java Views

| View | Route | Menu Order | Description |
|------|-------|-----------|-------------|
| `ComponentsView` | `/` (alias), `/components` | 1 | Large showcase: 20+ Vaadin components, Charts (live streaming), Map, Grid, form inputs |
| `FormView` | `/form` | 2 | Form with `BeanValidationBinder` + Jakarta validation |
| `GridView` | `/grid` | 3 | Grid with `PersonService` data provider and pagination |
| `ChatView` | `/chat` | 4 | Spring AI + OpenAI integration with file upload (`UploadDropZone`) |
| `PlaygroundView` | `/playground` | 5 | Simple text input + button interaction demo |
| `ShoppingListView` | `/shopping-list` | 6 | Signals-based reactive shopping list using `ListSignal` |
| `ExternalComponentView` | `/external-component` | 7 | Wraps React MUI Switch via `ReactAdapterElement` |
| `TShirtView` | `/tshirt` | — | T-shirt order form with `BeanValidationBinder` |
| `AiFilterView` | `/ai-filter` | 10 | Spring AI chat client with `@Tool` for filtering JAX talks by query |
| `ListOrdersView` | `/` | — | T-shirt orders grid (no menu entry) |

## Services (`service/`)

All annotated `@BrowserCallable @AnonymousAllowed`:

- `HelloReactWorldService` — `sayCiao(String)` greeting; used by the React hello-world view
- `PersonService` — extends `CrudRepositoryService`; used by `GridView` and `ComponentsView`
- `TShirtService` — CRUD for `TShirtOrder` entities
- `TalkService` — Provides JAX talk data; used by `AiFilterView`

## Key Configuration

- **Server port:** 8888 (`application.properties`)
- **H2 console:** Available at `/h2` in dev mode
- **Frontend tooling:** Vite + pnpm (shamefully-hoist enabled in `.npmrc`)
- **Frontend deps:** React 19, Hilla 25.1.3, MUI 7 (for Switch component)
- **Code style:** Prettier with single quotes, 120 char width (`.prettierrc.js`)
- **Vaadin allowed packages:** Explicitly listed in `application.properties` — add new packages there when integrating additional Vaadin add-ons

## Theme System

Seven themes are loaded dynamically in `MainLayout` via `ui.getPage().addStyleSheet()`:

1. **Default** — minimal overrides
2. **Carbon** — IBM Carbon Design inspired
3. **Less Carbon** — lighter Carbon variant
4. **Linear** — Linear app inspired (by Juuso)
5. **Sparkasse** — Sparkasse brand colors
6. **DHL** — DHL brand colors
7. **Deutsche Bank** — Deutsche Bank brand colors

Theme CSS files live in `src/main/resources/META-INF/resources/themes/`. The theme selector and dark mode toggle are in the `MainLayout` footer.