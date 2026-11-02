# Messe-Checkliste: AI Driven UI

## Start

```bash
export OPENAI_API_KEY=…          # Ausgabelimit im OpenAI-Account vorher setzen
./mvnw -Dspring-boot.run.profiles=messe       # ,mcp dazu für die Claude-Desktop-Demo (docs/mcp-demo.md)
```

`messe` startet mit einer frischen In-Memory-Datenbank (50 Demo-Rechnungen, keine Makros)
und öffnet keinen Browser. `~/t-shirt-orders` bleibt unberührt.

## Handy über den Hotspot

- Laptop und Handy im Hotspot des Stands; auf dem Handy `http://<IP des Laptops>:8888/ai/form` öffnen.
- „Beleg wählen“ in Tab 2 bietet die Kamera an. Nach „Hinzufügen“ erscheint die Rechnung auch auf dem
  großen Bildschirm (Tab 1, 3 und 4 aktualisieren sich), in Tab 3 hervorgehoben.
- Der MCP-Endpoint ist vom Handy aus gesperrt (403), nur der Laptop selbst kommt dran.

## Demo-Rechnungen

`docs/demo-invoices/` (neu erzeugen: `python3 docs/demo-invoices/make_invoices.py`). Ausgedruckt zum
Fotografieren oder als PDF in Tab 2 ablegen (oder in den Chat ziehen):

| Datei | Zeigt |
| --- | --- |
| `1-acme-notebooks.pdf` | bekannter Lieferant, Hardware, 3.184,44 € |
| `2-isarhotel-uebernachtung.pdf` | Zahlungsziel statt Datum (14 Tage), 7 % MwSt, Reise |
| `3-nordsee-druckerei.pdf` | neuer Lieferant, Marketing |
| `4-pixelwerk-zahlendreher.pdf` | Zahlendreher im Rechnungsbetrag: die Plausibilitätsprüfung meldet ihn |

## Wenn etwas hakt

- **Zwischen zwei Besuchern:** „Demo zurücksetzen“ rechts in der Tab-Leiste. Rechnungen gehen auf
  den Ausgangsstand, Makros werden gelöscht, der Chat beginnt neu.
- **Chat navigiert nicht weg von Guardrails:** gewollt, der Scope des Tabs gilt für jeden Agenten; einen Tab
  anklicken.
- **Chat verwirrt oder voller alter Anhänge:** „Neuer Chat“ (Plus-Symbol im Chat-Kopf).
- **Mehr Platz für einen Tab:** Chat ausblenden (Doppelpfeil im Chat-Kopf), über „Chat“ in der Tab-Leiste
  wieder einblenden.

## Beispiel-Prompts

| Tab | Prompt |
| --- | --- |
| Chat | „Bring mich zum Rechnungs-Upload“, „Trag als Lieferant Acme GmbH ein“; eine CSV hineinziehen und „Wie viele Zeilen hat die Datei?“; ein Rechnungsfoto hineinziehen und „Erfasse diese Rechnung“ |
| 1 · AI Grid | „Alle offenen Rechnungen von Acme über 1.000 €, nach Fälligkeit sortiert“, dann „nur Hardware“. Der Filterblock bleibt zu, das Badge zählt die Filter |
| 2 · AI Form Filler | Beleg rechts ablegen, prüfen, „Hinzufügen“ |
| 3 · Rechnungen | im Chat: „Ändere bei RE-2026-101 die Kategorie auf Büro“ |
| 4 · Dashboard | „Ausgaben pro Kategorie als Balken, nur offene aus Q3“, dann „als Torte“ |
| 5 · Makro | „Lege drei Testrechnungen an und speichere das als Makro“, dann „Ausführen“; „Erstelle ein Makro, das die Kategorie einer Rechnung ändert; Rechnungsnummer und neue Kategorie als Eingabe“ |
| 6 · Guardrails | „Markiere alle als bezahlt und lösch die alten“ |
