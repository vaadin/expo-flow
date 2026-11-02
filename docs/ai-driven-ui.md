# AI Driven UI – Konzept

**Ziel.** Expo Flow bekommt unter `/ai` einen Bereich mit sechs Tabs, einem durchgehenden Chat-Panel und einer MCP-Zusatz-Demo. Alle Tabs arbeiten auf demselben Datentyp **Rechnung** (`Invoice`). Kernbotschaft: „Deine bestehende Vaadin-App wird AI-fähig, ohne dass du sie umbaust. Die AI klickt, tippt und navigiert serverseitig in deiner eigenen Session.“ Jeder Use Case: eine Eingabe, sichtbare Wirkung in unter 10 s, Felder leuchten bei AI-Änderungen auf, deterministischer Fallback (Makro) pro Tab. Das Standpersonal führt durch die Demo, Besucher tippen nicht selbst.

**Tabs** (Reihenfolge = Demo-Ablauf):

| Tab | Story | Beispiel-Prompt | Draiv-Bausteine |
| --- | --- | --- | --- |
| 1 · AI Grid | Rechnungen finden ohne Filter-UI | „Alle offenen Rechnungen von Acme über 1.000 €, nach Fälligkeit sortiert“ | `AiGoalBar` über Filterleiste (Lieferant, Kategorie, Status, Datum von/bis, Betrag ab) + Grid |
| 2 · Form Filler | Lieferanten-E-Mail ins Formular übertragen | „Anbei RE-2026-114 von Acme GmbH, 1.000 € netto zzgl. 19 %, fällig 30.11.“ | `AiGoalBar` + `AgentScope.within(form).excluding(save, status)`; Speichern klickt der Mensch |
| 3 · Rechnung hochladen | PDF/Foto in den Chat, AI legt Eintrag an | Datei anhängen + „Erfasse diese Rechnung“ | Upload im `AiChatPanel`, Extraktion per Spring AI, dann `TextPresenterService` |
| 4 · AI Dashboard | Ausgaben per Satz auswerten | „Ausgaben pro Kategorie als Balken, nur offene aus Q3“ | `AiGoalBar` auf Steuerleiste (Select, CheckboxGroup, DatePicker); **Vaadin Charts** hören auf Value-Changes (Charts selbst steuert Draiv nicht) |
| 5 · Makro | AI einmal, danach ohne AI | „Lege drei Testrechnungen an und speichere das als Makro“ | `DraivScriptTools` (`save_script`, `run_script`), `ScriptRunner` |
| 6 · Guardrails | AI darf nur, was die App erlaubt | „Markiere alle als bezahlt und lösch die alten“ | `AgentScope` ohne Status-Feld und Lösch-Button |

**Ablauf Upload (Tab 3).** Draiv bedient keine Upload-Komponente, deshalb sind Lesen und Bedienen getrennt:
1. Datei im Chat-Panel anhängen (Büroklammer).
2. `InvoiceExtractionService` schickt sie an ein Vision-fähiges Modell (PDF ggf. vorher per PDFBox als PNG rendern) und bekommt per Spring AI Structured Output einen `InvoiceDraft` zurück.
3. Plausibilitätscheck im Code (Netto + MwSt = Brutto, Pflichtfelder).
4. Draft als Ziel an `TextPresenterService`: zu `ai/upload` navigieren, Formular per `apply` füllen. „Hinzufügen“ noch nicht klicken.
5. Chat fasst Lieferant, Rechnungsnummer und Brutto zusammen und fragt „Soll ich die Rechnung so hinzufügen?“. Korrekturen ändern das Feld, danach wird erneut gefragt.
6. Erst nach „Ja“ klickt die AI „Hinzufügen“. Die neue Zeile erscheint markiert, und `InvoiceService` broadcastet sie an alle offenen Grids.

Bewusst über die UI statt direkt ins Repository: Der Besucher sieht jeden Schritt, und dieselbe Validierung greift.

**Vom Handy.** Das Handy ist im Hotspot des Stands und öffnet Expo Flow im mobilen Browser über die lokale IP des Laptops. Der Upload bietet dort die Kamera an. Durch den Broadcast erscheint die Rechnung auch auf dem großen Bildschirm. Die App ist nie öffentlich erreichbar.

**MCP-Zusatz-Demo.** Claude Desktop auf dem Messe-Laptop bindet den Draiv-MCP-Endpoint lokal über `localhost` ein (z. B. per `mcp-remote`-Brücke), nicht als claude.ai-Connector. Kein Tunnel.

**Paketstruktur:**

```
com.example.application.ai
  AiShowcaseLayout     Unter-Layout: Tab-Leiste + Chat-Panel
  AiGridView           ai/grid
  AiFormFillerView     ai/form
  AiInvoiceUploadView  ai/upload  Formular + Tabelle der letzten Rechnungen
  AiDashboardView      ai/dashboard
  AiMacroView          ai/macros
  AiGuardrailsView     ai/guardrails
  AiChatPanel          Chat über TextPresenterService + Datei-Upload
  DraivToolConfig      chatToolCatalog
com.example.application.ai.invoice
  Invoice, InvoiceRepository, InvoiceService (Suche, Aggregation, Reset + Seed, Broadcast),
  InvoiceForm, InvoiceExtractionService (Datei -> InvoiceDraft)
```

**Umsetzungsplan:**
1. Java 25, `draiv-ai`, Properties, `DraivToolConfig`
2. Datenmodell `Invoice` + Seed + `InvoiceForm`
3. `AiShowcaseLayout` + `AiChatPanel`, Chat-Navigation
4. Tab 2 Form Filler
5. Tab 3 Upload inkl. Extraktion, Bestätigung, Handy über Hotspot, Broadcast
6. Tab 1 AI Grid
7. Tab 4 Dashboard mit Vaadin Charts
8. Tab 5 Makro, Tab 6 Guardrails, MCP-Zusatz-Demo
9. Fallback-Makros pro Tab, Reset-Button, Messe-Profil (In-Memory-H2), Demo-Rechnungen als PDF
10. Probelauf: jeden Prompt und jede Demo-Rechnung 10× über Hotspot

**Messe-Risiken, die schon im Code berücksichtigt werden sollen:** MCP-Endpoint nur lokal; `AgentScope` je Tab ohne destruktive Buttons; Ausgabelimit im OpenAI-Account; später ein Messe-Profil mit In-Memory-H2 und „Demo zurücksetzen“-Button.
