package com.guille.domain;

import java.util.Set;

public record Transaction(String date, String type, Float amount, Integer referenceNumber, String serial, String desc) {


    public Boolean descContains(String _desc) {
        return desc.toLowerCase().contains(_desc.toLowerCase());
    }

    public Boolean descContains(Set<String> _desc) {
        return _desc.stream().map(String::toLowerCase)
                .anyMatch(s -> desc.toLowerCase().contains(s));
    }

    @Override
    public String toString() {
        return "Account{" +
                "date='" + date + '\'' +
                ", type='" + type + '\'' +
                ", amount=" + amount +
                ", referenceNumber=" + referenceNumber +
                ", serial='" + serial + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }
}
