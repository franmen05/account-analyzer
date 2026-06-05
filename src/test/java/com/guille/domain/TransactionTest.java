package com.guille.domain;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionTest {

    private static Transaction withDesc(String desc) {
        return new Transaction("2024-01-01", "", 100f, 1, "", desc);
    }

    @Test
    void descContainsIsCaseInsensitiveForSingleString() {
        var t = withDesc("IMPUESTO SELECTIVO");

        assertTrue(t.descContains("impuesto"));
        assertTrue(t.descContains("SELECTIVO"));
        assertFalse(t.descContains("comision"));
    }

    @Test
    void descContainsMatchesAnyOfASet() {
        var t = withDesc("CARGO POR SERVICIO");

        assertTrue(t.descContains(Set.of("interes", "cargo por serv")));
        assertFalse(t.descContains(Set.of("mora", "impuesto")));
    }

    @Test
    void descContainsAgainstEmptySetIsFalse() {
        assertFalse(withDesc("ANYTHING").descContains(Set.of()));
    }
}
