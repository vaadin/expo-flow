# Vaadin expo playground

This is a playground [Vaadin Flow](https://vaadin.com).

## Requirements
- Java 21 or later
- An [OpenAI API key](https://platform.openai.com/account/api-keys) as an environment variable `OPENAI_API_KEY` if you want to use the Chat view

## Running the application
Run the app by running `Application.java` or with the default Maven goal:

```
mvn
```

The app starts on [http://localhost:8888](http://localhost:8888). Override the
port with the `PORT` environment variable.

For a production build:

```
mvn clean package -Pproduction
```

## Theming

The app uses [Aura](https://vaadin.com/docs/latest/styling/themes/aura), the
default Vaadin 25 theme. It is loaded from `Application.java` together with the
app's own stylesheet:

```java
@StyleSheet(Aura.STYLESHEET)
@StyleSheet("styles.css")
```

All CSS lives in `src/main/resources/META-INF/resources/`. `styles.css` is the
master stylesheet and imports the per-view files.

The drawer footer has three controls:

- **Theme** — swaps the active theme at runtime
- **Dark mode** — toggles the CSS `color-scheme` between light and dark
- **Unicorn mode** — cycles the Aura accent and background colours through the
  hue spectrum (`src/main/frontend/color-cycle.js`)

Themes live in `META-INF/resources/themes/` and customise Aura's high-level
custom properties: Default, Carbon, Less Carbon, Linear, Material, Sparkasse,
DHL and Deutsche Bank. To add one, drop a `<name>-theme.css` next to the others
and add its display name to the ComboBox in `MainLayout.createFooter()`.

## Testing

```
mvn test
```

Tests use [browserless testing](https://vaadin.com/docs/latest/testing), which
drives the views in-process — no browser and no running server. See
`PlaygroundViewTest` for the pattern: extend `SpringBrowserlessTest`, call
`navigate()`, then query components with `$()` and interact through `test()`.
