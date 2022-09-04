package com.guille.domain;

public record Summary(TransactionSummary interest, TransactionSummary taxes, TransactionSummary nonPaymentFee,
                      TransactionSummary commissions, Float total) {

    public static Summary build(TransactionSummary interest, TransactionSummary taxes, TransactionSummary nonPaymentFee,
                                TransactionSummary commissions){
        var total=interest.total() + taxes.total() + nonPaymentFee.total() + commissions.total();
        return new Summary(interest,taxes,nonPaymentFee,commissions,total);
    }
}