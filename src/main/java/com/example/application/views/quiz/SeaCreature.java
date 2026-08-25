package com.example.application.views.quiz;

public enum SeaCreature {
    Octopus("Octopus", "~5000 m", "Cephalopod", true),
    Squid("Squid", "~4000 m", "Cephalopod", true),
    Nautilus("Nautilus", "~700 m", "Cephalopod", true),
    Jellyfish("Jellyfish", "~10000 m", "Cnidarian", false),
    Crab("Crab", "~5000 m", "Crustacean", false);

    private final String name;
    private final String depth;
    private final String type;
    private final boolean correct;

    SeaCreature(String name, String depth, String type, boolean correct) {
        this.name = name;
        this.depth = depth;
        this.type = type;
        this.correct = correct;
    }

    public String getName() {
        return name;
    }

    public String getDepth() {
        return depth;
    }

    public String getType() {
        return type;
    }

    public boolean isCorrect() {
        return correct;
    }
}
