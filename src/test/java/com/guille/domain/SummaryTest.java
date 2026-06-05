package com.guille.domain;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SummaryTest {

    private static TransactionSummary ts(float total) {
        return new TransactionSummary(Set.of(), total);
    }

    @Test
    void totalSumsAllCategoriesExceptUserInterestOutTotal() {
        var summary = Summary.build(
                ts(10f),   // interest
                ts(20f),   // taxes
                ts(30f),   // nonPaymentFee
                ts(40f),   // commissions
                ts(50f),   // userInterest
                ts(999f)); // userInterestOutTotal -> must be excluded from total

        assertEquals(150f, summary.total(), 0.0001f);
    }

    @Test
    void userInterestOutTotalIsReportedButNotAddedToTotal() {
        var summary = Summary.build(ts(0f), ts(0f), ts(0f), ts(0f), ts(0f), ts(123f));

        assertEquals(0f, summary.total(), 0.0001f);
        assertEquals(123f, summary.userInterestOutTotal().total(), 0.0001f);
    }
}
