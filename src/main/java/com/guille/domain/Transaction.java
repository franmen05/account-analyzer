package com.guille.domain;

public record Transaction(String date, String type, Float amount , Integer referenceNumber, String serial, String desc) {


//    public Boolean compareDesc(String _desc) {
//        return desc.equalsIgnoreCase(_desc);
//    }
//    public Boolean isSerial(String _serial) {
//        return serial.equalsIgnoreCase(_serial);
//    }
    public Boolean descContains(String _desc) {
        return desc.toLowerCase().contains(_desc.toLowerCase());
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
