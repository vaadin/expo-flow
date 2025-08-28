package com.example.application.views.quiz;

public enum Battle {
    Cannae("Cannae", "216 BC", "Second Punic War", true),
    Alesia("Alesia", "52 BC", "Gallic Wars", false),
    Zama("Zama", "202 BC", "Second Punic War", true),
    Actium("Actium", "32 BC", "Final War of the Roman Republic", false),
    Mylae("Mylae", "260 BC", "First Punic War", true);

    private final String name;
    private final String start;
    private final String war;
    private final boolean correct;

    Battle(String name, String start, String war, boolean correct) {
        this.name = name;
        this.start = start;
        this.war = war;
        this.correct = correct;
    }

    public String getName() {
        return name;
    }

    public String getStart() {
        return start;
    }

    public String getWar() {
        return war;
    }

    public boolean isCorrect() {
        return correct;
    }
}
