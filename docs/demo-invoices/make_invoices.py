"""Writes the demo invoices for the upload tab (docs/ai-driven-ui.md, tab 3) as PDFs.

No dependencies: a minimal PDF with the standard Helvetica fonts in WinAnsiEncoding, so umlauts and
the euro sign work. Run from anywhere: python3 make_invoices.py; the PDFs land next to this script.
All companies and the recipient are made up.
"""
from decimal import Decimal
from pathlib import Path

RECIPIENT = ["Expo Flow Demo GmbH", "Buchhaltung", "Messeweg 1", "80331 München"]


def euro(amount):
    text = f"{amount:,.2f}".replace(",", "X").replace(".", ",").replace("X", ".")
    return text + " €"


class Page:
    def __init__(self):
        self.ops = []

    def text(self, x, y, value, size=10, bold=False, right=False):
        if right:
            x -= width(value, size, bold)
        escaped = value.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)")
        self.ops.append(f"BT /{'F2' if bold else 'F1'} {size} Tf {x:.1f} {y:.1f} Td ({escaped}) Tj ET")

    def line(self, x1, y1, x2, y2):
        self.ops.append(f"{x1} {y1} m {x2} {y2} l S")

    def stream(self):
        return "\n".join(self.ops).encode("cp1252")


def width(value, size, bold):
    """Rough Helvetica width, good enough to right-align amounts."""
    return len(value) * size * (0.56 if bold else 0.52)


def write_pdf(path, page):
    content = page.stream()
    objects = [
        b"<< /Type /Catalog /Pages 2 0 R >>",
        b"<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
        b"<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
        b"/Resources << /Font << /F1 4 0 R /F2 5 0 R >> >> /Contents 6 0 R >>",
        b"<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>",
        b"<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>",
        b"<< /Length " + str(len(content)).encode() + b" >>\nstream\n" + content + b"\nendstream",
    ]
    out = bytearray(b"%PDF-1.4\n")
    offsets = []
    for number, body in enumerate(objects, start=1):
        offsets.append(len(out))
        out += f"{number} 0 obj\n".encode() + body + b"\nendobj\n"
    xref = len(out)
    out += f"xref\n0 {len(objects) + 1}\n0000000000 65535 f \n".encode()
    for offset in offsets:
        out += f"{offset:010d} 00000 n \n".encode()
    out += f"trailer\n<< /Size {len(objects) + 1} /Root 1 0 R >>\nstartxref\n{xref}\n%%EOF\n".encode()
    path.write_bytes(bytes(out))


def invoice(path, sender, number, date, due_line, items, vat_percent, gross_printed=None):
    """items: (quantity, description, unit price). gross_printed overrides the total, for a typo."""
    page = Page()
    page.text(50, 790, sender[0], 16, bold=True)
    for i, line in enumerate(sender[1:]):
        page.text(50, 772 - i * 13, line, 9)
    page.text(50, 700, f"{sender[0]} · {sender[1]} · {sender[2]}", 7)
    for i, line in enumerate(RECIPIENT):
        page.text(50, 685 - i * 13, line)
    page.text(400, 685, "Rechnungsnummer:", 9)
    page.text(545, 685, number, 9, bold=True, right=True)
    page.text(400, 672, "Rechnungsdatum:", 9)
    page.text(545, 672, date, 9, right=True)

    page.text(50, 590, "Rechnung " + number, 14, bold=True)
    page.text(50, 560, "Pos.", 9, bold=True)
    page.text(85, 560, "Menge", 9, bold=True)
    page.text(130, 560, "Beschreibung", 9, bold=True)
    page.text(460, 560, "Einzelpreis", 9, bold=True, right=True)
    page.text(545, 560, "Betrag", 9, bold=True, right=True)
    page.line(50, 553, 545, 553)
    net = Decimal("0")
    y = 538
    for position, (quantity, description, price) in enumerate(items, start=1):
        amount = quantity * price
        net += amount
        page.text(50, y, str(position), 9)
        page.text(85, y, str(quantity), 9)
        page.text(130, y, description, 9)
        page.text(460, y, euro(price), 9, right=True)
        page.text(545, y, euro(amount), 9, right=True)
        y -= 16
    page.line(330, y + 4, 545, y + 4)
    vat = (net * vat_percent / 100).quantize(Decimal("0.01"))
    gross = gross_printed if gross_printed is not None else net + vat
    for label, value, bold in (("Nettobetrag", net, False), (f"zzgl. {vat_percent} % MwSt.", vat, False),
                               ("Rechnungsbetrag", gross, True)):
        y -= 14
        page.text(340, y, label, 10, bold=bold)
        page.text(545, y, euro(value), 10, bold=bold, right=True)

    page.text(50, y - 50, due_line, 10)
    page.text(50, y - 66, "Bitte überweisen Sie den Betrag unter Angabe der Rechnungsnummer.", 9)
    page.line(50, 80, 545, 80)
    page.text(50, 66, f"{sender[0]} · IBAN DE00 0000 0000 0000 0000 00 · USt-IdNr. DE000000000 (Demo)", 7)
    write_pdf(path, page)


if __name__ == "__main__":
    here = Path(__file__).parent
    # 1: a known supplier, hardware, a due date; the upload tab's fallback macro enters the same values
    invoice(here / "1-acme-notebooks.pdf",
            ["Acme GmbH", "Industriestraße 12", "70565 Stuttgart"],
            "RE-2026-131", "27.10.2026", "Zahlbar bis 26.11.2026 ohne Abzug.",
            [(2, 'Notebook 14", 32 GB RAM', Decimal("1149.00")), (2, "Dockingstation USB-C", Decimal("189.00"))],
            19)
    # 2: a payment term instead of a due date, and the reduced rate
    invoice(here / "2-isarhotel-uebernachtung.pdf",
            ["Isarhotel München", "Am Isarkanal 3", "81379 München"],
            "2026-0871", "30.10.2026", "Zahlbar innerhalb von 14 Tagen nach Rechnungsdatum.",
            [(3, "Übernachtung Einzelzimmer, W-JAX", Decimal("129.00"))],
            7)
    # 3: a supplier the app does not know yet
    invoice(here / "3-nordsee-druckerei.pdf",
            ["Nordsee Druckerei GmbH", "Hafenstraße 40", "26382 Wilhelmshaven"],
            "ND-44817", "29.10.2026", "Fällig am 28.11.2026.",
            [(5, "Messeflyer DIN A5, 4/4-farbig, Paket à 1.000 Stück", Decimal("96.00")), (2, "Roll-up Display 85 x 200 cm", Decimal("95.00"))],
            19)
    # 4: a typo in the total (1.482,00 instead of 1.428,00): the plausibility check reports it
    invoice(here / "4-pixelwerk-zahlendreher.pdf",
            ["Pixelwerk Agentur", "Kreativkai 7", "20457 Hamburg"],
            "PW-2026-58", "31.10.2026", "Zahlbar bis 30.11.2026.",
            [(1, "Landingpage W-JAX-Kampagne", Decimal("1200.00"))],
            19, gross_printed=Decimal("1482.00"))
