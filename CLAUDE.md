# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Vaadin 25 playground application demonstrating both Java-based Flow views and React-based Hilla client-side views. Single-module Maven project with Spring Boot 4, Java 25, and H2 database.

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
  - `views/MainLayout.java` — App shell with `@Layout`, uses `AppLayout` + `SideNav`
  - `service/` — Spring services; `@BrowserCallable` services are exposed to React/Hilla frontend
  - `data/entity/` — JPA entities (`Person`, `TShirtOrder`) extending `AbstractEntity`
  - `data/repository/` — Spring Data JPA repositories
  - `data/generator/` — Sample data loaded on startup
- `src/main/frontend/` — Frontend code
  - `views/` — React views using Hilla file-based routing
  - `themes/myapp/` — Custom CSS styles
- `Application.java` — Entry point; uses Aura theme (`@StyleSheet(Aura.STYLESHEET)`), `@Push` for WebSocket support, `@EnableScheduling`

**Routing:** Views use `@Route` with `@Menu(order=N)` for automatic navigation menu generation via `MenuConfiguration`. The `ComponentsView` is aliased to `/` as the default route.

**Data layer:** H2 file-based database (`~/t-shirt-orders`), Spring Data JPA with auto DDL, Jakarta Bean Validation.

**AI integration:** ChatView uses Spring AI with OpenAI (`OPENAI_API_KEY` env var required).

## Key Configuration

- **Server port:** 8888 (`application.properties`)
- **H2 console:** Available at `/h2` in dev mode
- **Frontend tooling:** Vite + pnpm (shamefully-hoist enabled in `.npmrc`)
- **Code style:** Prettier with single quotes, 120 char width (`.prettierrc.js`)
- **Vaadin allowed packages:** Explicitly listed in `application.properties` — add new packages there when integrating additional Vaadin add-ons
