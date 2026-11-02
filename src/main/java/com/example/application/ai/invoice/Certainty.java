package com.example.application.ai.invoice;

/** How sure the extraction is about one value read off a receipt. */
public enum Certainty {
    /** Printed plainly on the receipt and read as is. */
    HIGH("hoch"),
    /** Read with some doubt (blurry, ambiguous), or derived from what is printed (a due date from a term). */
    MEDIUM("mittel"),
    /** Guessed or inferred (a category from the items), or contradicted by the plausibility check. */
    LOW("niedrig");

    private final String label;

    Certainty(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    /** The less certain of the two. */
    public Certainty atMost(Certainty other) {
        return other != null && other.ordinal() > ordinal() ? other : this;
    }
}
