package com.example.stattube;

public class ComparisonItem {
    final String label;
    final String value1;
    final String value2;
    final long rawValue1;
    final long rawValue2;

    public ComparisonItem(String label, String value1, String value2, long rawValue1, long rawValue2) {
        this.label = label;
        this.value1 = value1;
        this.value2 = value2;
        this.rawValue1 = rawValue1;
        this.rawValue2 = rawValue2;
    }
}