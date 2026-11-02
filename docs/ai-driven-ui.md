# AI Driven UI – Konzept

**Ziel.** Expo Flow hat unter `/ai` einen Bereich mit sechs Tabs, einem durchgehenden Chat-Panel und einer MCP-Zusatz-Demo. Alle Tabs arbeiten auf demselben Datentyp **Rechnung** (`Invoice`). Kernbotschaft: „Deine bestehende Vaadin-App wird AI-fähig, ohne dass du sie umbaust. Die AI klickt, tippt und navigiert serverseitig in deiner eigenen Session.“ Jeder Use Case: eine Eingabe, sichtbare Wirkung in unter 10 s, Felder leuchten bei AI-Änderungen auf (im zugeklappten Filterblock pulsiert stattdessen dessen Überschrift). Das Standpersonal führt durch die Demo, Besucher tippen nicht selbst.

**Tabs** (Reihenfolge = Demo-Ablauf):

| Tab | Story | Beispiel-Prompt | Draiv-Bausteine |
| --- | --- | --- | --- |
| 1 · AI Grid | Rechnungen finden ohne Filter-UI | „Alle offenen Rechnungen von Acme über 1.000 €, nach Fälligkeit sortiert“ | `AiPromptBar` (`UiAssistant` + `AgentScope.within(filters)`) über einer zugeklappten Filterleiste (`FilterDetails`: Badge mit aktiven Filtern, Pulsieren bei Änderung) + Grid |
| 2 · AI Form Filler | Beleg ablegen, AI füllt das Formular; pro Feld sieht man, wie sicher der Wert ist und woher er im Beleg stammt | PDF oder Foto rechts ablegen | `InvoiceExtractionService` liest den Beleg samt Sicherheit und Herkunft je Feld, dann `UiAssistant` mit `AgentScope.within(form).excluding(add, status)`; „Hinzufügen“ klickt der Mensch |
| 3 · Rechnungen | Alle Rechnungen pflegen | im Chat: „Ändere bei RE-2026-101 die Kategorie auf Büro“ | Master-Detail aus Suchfeld, Grid und `InvoiceForm.editor`; draiv wählt die Zeile per `selectable/selectByContentMatching`. Status und Löschen nur für Menschen |
| 4 · AI Dashboard | Ausgaben per Satz auswerten | „Ausgaben pro Kategorie als Balken, nur offene aus Q3“ | `AiPromptBar` auf die zugeklappte Steuerleiste (`FilterDetails`: Select, CheckboxGroup, DatePicker); **Vaadin Charts** hören auf Value-Changes (Charts selbst steuert Draiv nicht) |
| 5 · Makro | AI einmal, danach ohne AI | „Lege drei Testrechnungen an und speichere das als Makro“; „Erstelle ein Makro, das die Kategorie einer Rechnung ändert; Rechnungsnummer und neue Kategorie als Eingabe“ | `DraivScriptTools` (`save_script`, `run_script`), `ScriptRunner`; Makros legen über Tab 3 an und ändern, löschen nie |
| 6 · Guardrails | AI darf nur, was die App erlaubt | „Markiere alle als bezahlt und lösch die alten“ | `AgentScope` ohne Bezahlt- und Lösch-Button, für jeden Agenten auf der UI; Handler prüfen `CurrentActor` |

**Chat-Panel.** Rechts in jedem Tab, über `TextPresenterService.respond(...)` mit Verlauf. Es navigiert zwischen den Tabs und bedient sie (`inspect`, `apply`, `navigate`). Dateien (Bilder, PDF, CSV, Markdown, Text, bis zu 3) per Drag & Drop aufs Panel oder per Büroklammer; sie warten als Chips und gehen mit der nächsten Nachricht mit. Bilder und PDFs gehen als Anhang ans Modell, Textdateien als Text in die Nachricht. „Neuer Chat“ vergisst Verlauf und Anhänge; das Panel lässt sich aus- und wieder einblenden.

**Ablauf Beleg (Tab 2).** Draiv bedient keine Upload-Komponente, deshalb sind Lesen und Bedienen getrennt:
1. PDF oder Foto rechts ablegen oder wählen; die Vorschau erscheint (PDF eingebettet, Bild als Bild).
2. `InvoiceExtractionService` schickt die Datei außerhalb des UI-Threads an das Modell (PDFs direkt als Datei-Part, ohne Rendern) und bekommt per Structured Output einen `InvoiceDraft`, pro Feld mit **Sicherheit** (hoch, mittel, niedrig) und der **Herkunft im Beleg** (das Zitat, aus dem der Wert stammt).
3. Plausibilitätscheck im Code (Netto + MwSt = Brutto, Pflichtfelder, Fälligkeit); was er widerlegt, sinkt auf „niedrig“. Probleme und unsicher gelesene Felder stehen über dem Formular, Sicherheit und Zitat unter jedem Feld.
4. Der `UiAssistant` füllt das Formular mit dem Draft, beschränkt auf die Formularfelder. Die Felder leuchten auf.
5. Der Mensch prüft und klickt „Hinzufügen“. Formular und Vorschau leeren sich, `InvoiceService` meldet die neue Rechnung an alle offenen Tabs.

Über den Chat geht es auch: Rechnung anhängen, „Erfasse diese Rechnung“. Die AI liest die Datei selbst, füllt das Formular in Tab 2 und fragt vor „Hinzufügen“ nach.

Bewusst über die UI statt direkt ins Repository: Der Besucher sieht jeden Schritt, und dieselbe Validierung greift.

**Vom Handy.** Das Handy ist im Hotspot des Stands und öffnet `http://<IP des Laptops>:8888/ai/form`. Der Upload bietet dort die Kamera an. Durch den Broadcast erscheint die Rechnung auch auf dem großen Bildschirm. Die App ist nie öffentlich erreichbar.

**MCP-Zusatz-Demo.** Claude Desktop auf dem Messe-Laptop bindet den Draiv-MCP-Endpoint lokal über `localhost` ein (per `mcp-remote`-Brücke), nicht als claude.ai-Connector. Kein Tunnel. Details: `docs/mcp-demo.md`.

**Paketstruktur:**

```
com.example.application.ai
  AiShowcaseLayout     Unter-Layout: Tab-Leiste, „Demo zurücksetzen“, Chat ein-/ausblenden
  AiGridView           ai/grid
  AiFormFillerView     ai/form       Formular + Belegvorschau mit Upload
  AiInvoiceCrudView    ai/invoices   Suche, Grid, Editor
  AiDashboardView      ai/dashboard
  AiMacroView          ai/macros
  AiGuardrailsView     ai/guardrails
  AiChatPanel          Chat über TextPresenterService, Drop-Zone für Anhänge
  AiPromptBar          Prompt + Button über UiAssistant mit AgentScope
  FilterDetails        zugeklappte Filter mit Badge und Pulsieren
  InvoiceDraftGoal     Ziel: Draft ins Formular übertragen
  DraivToolConfig      chatToolCatalog; InMemoryScriptStore für Makros
com.example.application.ai.invoice
  Invoice, InvoiceRepository, InvoiceService (Suche, Aggregation, Speichern, Löschen, Reset + Seed,
  Broadcast als InvoiceChange), InvoiceForm (Anlegen und Bearbeiten), InvoiceFilter, InvoiceSort,
  InvoiceGrouping, InvoiceExtractionService (Datei -> InvoiceDraft mit FieldEvidence: Certainty + Zitat)
com.example.application.ai.mcp
  McpDemoConfig        Profil mcp: UI-Tools per MCP, nur von localhost
```

**Messe-Risiken, im Code berücksichtigt:** MCP-Endpoint nur lokal; Status und Löschen sind für jeden Agenten unerreichbar (`AgentScope` bzw. `data-draiv-control`, dazu `CurrentActor` in den Handlern, weil Makros keinen Scope kennen); Messe-Profil mit In-Memory-H2 und „Demo zurücksetzen“. Offen: Ausgabelimit im OpenAI-Account setzen.
