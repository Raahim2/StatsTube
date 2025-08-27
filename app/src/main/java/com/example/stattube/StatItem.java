package com.example.stattube;

public class StatItem {
    private final String label;
    private final String value;

    public StatItem(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }
}