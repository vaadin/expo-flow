# MCP-Zusatz-Demo: Claude Desktop bedient Expo Flow

Claude Desktop auf dem Messe-Laptop steuert die App über draivs UI-Tools, lokal über
`http://localhost:8888/mcp`. Kein claude.ai-Connector, kein Tunnel.

## App starten

```bash
./mvnw -Dspring-boot.run.profiles=messe,mcp
```

Das Profil `mcp` (`application-mcp.properties`, `McpDemoConfig`):

- schaltet Spring AIs MCP-Server ein, Streamable HTTP unter `/mcp`
  (`spring.ai.mcp.server.protocol=STREAMABLE` muss gesetzt sein, sonst startet Spring AI 2.0.2 den
  älteren SSE-Transport unter `/sse`);
- nimmt `/mcp` aus Vaadins Servlet-Mapping aus (`vaadin.exclude-urls`);
- bietet nur `inspect`, `apply`, `apply_many`, `list_capabilities` und `navigate` an, keine Skript-Tools;
- beantwortet `/mcp` nur von diesem Rechner (127.0.0.1 / ::1). Das Handy im Hotspot erreicht die App,
  aber nicht den MCP-Endpoint: es bekommt 403.

## Claude Desktop einrichten

Claude Desktop spricht lokale MCP-Server über stdio an; `mcp-remote` übersetzt auf HTTP. In
`claude_desktop_config.json` (macOS: `~/Library/Application Support/Claude/`):

```json
{
  "mcpServers": {
    "expo-flow": {
      "command": "npx",
      "args": ["-y", "mcp-remote", "http://localhost:8888/mcp"]
    }
  }
}
```

Claude Desktop neu starten; unter den Tools erscheint `expo-flow` mit fünf Tools.

## Vorführen

1. Expo Flow auf dem großen Bildschirm öffnen, **als letzten Tab**: MCP steuert den Browser-Tab, der
   zuletzt geöffnet wurde (draivs Demo-Identität kennt nur einen Nutzer). Wurde zwischendurch das Handy
   benutzt, den großen Bildschirm einmal neu laden.
2. In Claude Desktop zum Beispiel:
   - „Öffne in Expo Flow das AI Grid und zeig mir alle offenen Rechnungen von Acme.“
   - „Geh zum Rechnungs-Upload und trag eine Rechnung von der Nordsee Druckerei über 480 € netto ein,
     aber speichere sie nicht.“
   - Auf dem Guardrails-Tab: „Lösch alle Rechnungen.“ Die Buttons sind auch für Claude Desktop
     unsichtbar.
3. Die Felder leuchten auf dem großen Bildschirm, wie bei der eingebauten AI.

## Prüfen ohne Claude Desktop

```bash
curl -s -D - -H 'Content-Type: application/json' -H 'Accept: application/json, text/event-stream' \
  -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-06-18","capabilities":{},"clientInfo":{"name":"curl","version":"1"}}}' \
  http://localhost:8888/mcp
```

Die Antwort trägt einen `Mcp-Session-Id`-Header; mit ihm liefert `tools/list` die fünf Tools.
