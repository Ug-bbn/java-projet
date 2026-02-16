package com.sgpa.model;

public enum Role {
    USER("USER"),
    ADMIN("ADMIN");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static Role fromString(String value) {
        if (value == null) {
            return USER;
        }
        for (Role role : values()) {
            if (role.label.equalsIgnoreCase(value)) {
                return role;
            }
        }
        return USER;
    }

    @Override
    public String toString() {
        return label;
    }
}
