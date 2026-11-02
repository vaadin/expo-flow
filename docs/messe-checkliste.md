# Messe-Checkliste: AI Driven UI

## Start

```bash
export OPENAI_API_KEY=…          # Ausgabelimit im OpenAI-Account vorher setzen
./mvnw -Dspring-boot.run.profiles=messe       # ,mcp dazu für die Claude-Desktop-Demo (docs/mcp-demo.md)
```

`messe` startet mit einer frischen In-Memory-Datenbank (50 Demo-Rechnungen, keine Makros)
und öffnet keinen Browser. `~/t-shirt-orders` bleibt unberührt.

## Handy über den Hotspot

- Laptop und Handy im Hotspot des Stands; auf dem Handy `http://<IP des Laptops>:8888/ai/upload` öffnen.
- Die Büroklammer im Chat bietet die Kamera an. Eine hochgeladene Rechnung erscheint nach dem „Ja“ auch
  auf dem großen Bildschirm, hervorgehoben.
- Der MCP-Endpoint ist vom Handy aus gesperrt (403), nur der Laptop selbst kommt dran.

## Demo-Rechnungen

`docs/demo-invoices/` (neu erzeugen: `python3 docs/demo-invoices/make_invoices.py`). Ausgedruckt zum
Fotografieren oder als PDF hochladen:

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

## Beispiel-Prompts

| Tab | Prompt |
| --- | --- |
| Chat | „Bring mich zum Rechnungs-Upload“, „Trag als Lieferant Acme GmbH ein“ |
| 1 · AI Grid | „Alle offenen Rechnungen von Acme über 1.000 €, nach Fälligkeit sortiert“, dann „nur Hardware“ |
| 2 · Form Filler | „Beispiel-E-Mail“, dann „Formular ausfüllen“ |
| 3 · Upload | Rechnung anhängen, Korrektur („Lieferant ist …“), dann „Ja“ |
| 4 · Dashboard | „Ausgaben pro Kategorie als Balken, nur offene aus Q3“, dann „als Torte“ |
| 5 · Makro | „Lege drei Testrechnungen an und speichere das als Makro“, dann „Ausführen“ |
| 6 · Guardrails | „Markiere alle als bezahlt und lösch die alten“ |
