package com.guille.domain;

public record Transaction(String date, String type, Float amount , Integer referenceNumber, String serial, String desc) {


    public Boolean compareDesc(String _desc) {
        return desc.equalsIgnoreCase(_desc);
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
