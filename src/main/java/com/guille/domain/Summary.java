package com.guille.domain;

public record Summary(TransactionSummary interest, TransactionSummary taxes, TransactionSummary nonPaymentFee,
                      TransactionSummary commissions,TransactionSummary userInterest, Float total) {

    public static Summary build(TransactionSummary interest, TransactionSummary taxes, TransactionSummary nonPaymentFee,
                                TransactionSummary commissions,TransactionSummary userInterest){
        var total=interest.total() + taxes.total() + nonPaymentFee.total() + commissions.total()+userInterest.total();
        return new Summary(interest,taxes,nonPaymentFee,commissions,userInterest,total);
    }
}