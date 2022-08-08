package com.guille.domain;

public record Transaction(String date, String type, Float amount , Integer referenceNumber, String serial, String Desc) {

    @Override
    public String toString() {
        return "Account{" +
                "date='" + date + '\'' +
                ", type='" + type + '\'' +
                ", amount=" + amount +
                ", referenceNumber=" + referenceNumber +
                ", serial='" + serial + '\'' +
                ", Desc='" + Desc + '\'' +
                '}';
    }
}
