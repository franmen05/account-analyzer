package com.guille.domain;

public record TransactionSummary(Float interest, Float taxes, Float nonPaymentFee,Float commissions,Float total) {

    public static TransactionSummary build(Float interest, Float taxes, Float nonPaymentFee,Float commissions){
        var total=interest+taxes+nonPaymentFee+commissions;
        return new TransactionSummary(interest,taxes,nonPaymentFee,commissions,total);
    }
}
