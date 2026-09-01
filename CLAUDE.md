# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **Vaadin 25.1 full-stack demo application** showcasing Java Flow views, Hilla React views, Spring AI integration, and Vaadin Signals. It demonstrates a hybrid architecture where both server-side Java components and client-side React components coexist.

## Commands

```bash
mvn                        # Run in development mode (default goal: spring-boot:run)
mvn clean package          # Production build (includes frontend bundle)
mvn -Pit integration-test  # Run integration tests (starts server + Playwright)
```

The app runs on port **8888**. The Chat view requires `OPENAI_API_KEY` env var.

## Architecture

### Hybrid View Model

Two types of views coexist under the same router:

- **Java Flow views** (`src/main/java/.../views/`): Server-rendered components with `@Route` and `@Menu` annotations. The layout, navigation, and state live on the server.
- **React/Hilla views** (`src/main/frontend/views/`): Client-rendered TSX files. The file-router auto-registers them based on file path.

`MainLayout.java` is the shared shell (AppLayout + SideNav) used by all Java views. React views render into the Hilla outlet.

### Client–Server Communication (Hilla)

Java service classes annotated with `@BrowserCallable` + `@AnonymousAllowed` are automatically exposed as type-safe TypeScript endpoints. The Hilla codegen runs during `mvn` and outputs to `src/main/frontend/generated/`:
- `endpoints.ts` — callable TypeScript functions for each Java method
- `routes.tsx` — auto-generated React routes
- Type models for all Java DTOs

**Never manually edit files in `src/main/frontend/generated/` or `vite.generated.ts`** — they are overwritten on every build.

### Signals / Reactive State

`ShoppingListView.java` demonstrates **Vaadin Signals** (`ListSignal`, computed signals, `bindChildren()`, `bindValue()`). The `@vaadin/hilla-react-signals` package is the React counterpart. This is a key differentiator of Vaadin 25 — signals provide reactive state sync between client and server.

### Data Layer

- Spring Data JPA repositories with H2 (in-memory dev, file-based via `~/t-shirt-orders`)
- `DataGenerator.java` populates demo data on startup via `CommandLineRunner`
- Base entity: `AbstractEntity.java` (UUID primary key)
- Validation: Jakarta annotations on entities + `BeanValidationBinder` in forms

### AI Integration

`ChatView.java` uses Spring AI (`ChatModel`) via `AIOrchestrator` with file upload support (images, PDFs, text; max 5 files, 5 MB each). The OpenAI model is configured via `application.properties`.

## Key Configuration

- **Port**: `server.port=8888` in `application.properties`
- **Prettier**: single quotes, 120-char line width (`.prettierrc.js`)
- **pnpm**: `shamefully-hoisted=true` (flat node_modules)
- **TypeScript paths**: `Frontend/*` maps to `src/main/frontend/*`
- **Vaadin allowed packages**: whitelist in `application.properties` controls which Java packages Hilla can expose

## Testing

Tests use `SpringBrowserlessTest` (Playwright-based, headless). See `PlaygroundViewTest.java` for the pattern: inject `@Autowired SpringBrowserlessTest test`, call `test.navigate()`, then query components with `test.$()`.

Run only integration tests: `mvn -Pit integration-test`
