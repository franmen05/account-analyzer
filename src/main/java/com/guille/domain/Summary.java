package com.guille.domain;

public record Summary(TransactionSummary interest, Float taxes, Float nonPaymentFee, Float commissions, Float total) {

    public static Summary build(TransactionSummary interest, Float taxes, Float nonPaymentFee, Float commissions){
        var total=interest.total()+taxes+nonPaymentFee+commissions;
        return new Summary(interest,taxes,nonPaymentFee,commissions,total);
    }
}
