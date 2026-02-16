package com.sgpa.model;

public enum StatutCommande {
    EN_COURS("EN_COURS"),
    RECUE("RECUE");

    private final String label;

    StatutCommande(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static StatutCommande fromString(String value) {
        if (value == null) {
            return EN_COURS;
        }
        for (StatutCommande statut : values()) {
            if (statut.label.equalsIgnoreCase(value)) {
                return statut;
            }
        }
        return EN_COURS;
    }

    @Override
    public String toString() {
        return label;
    }
}
