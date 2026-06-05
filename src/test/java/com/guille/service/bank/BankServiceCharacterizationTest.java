package com.guille.service.bank;

import com.guille.domain.Deduction;
import com.guille.domain.DeductionType;
import com.guille.domain.Transaction;
import com.guille.domain.TransactionSummary;
import com.guille.repository.DeductionRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests: they pin down the CURRENT behaviour of the bank
 * services' getTransactionSummary so a later refactor can be proven
 * behaviour-preserving. They intentionally exercise the public API (keyword
 * matching, DB-rule union, and the USER_INTEREST_OUT_TOTAL formatting) rather
 * than internals. The test datasource is an empty in-memory H2, so without a
 * persisted rule only the hard-coded keywords match.
 */
@QuarkusTest
class BankServiceCharacterizationTest {

    // Inject the leaf beans: BHDAccountService / PopularAccountService have PDF
    // subclasses, so injecting them directly is an ambiguous CDI resolution.
    // The PDF leaves inherit getTransactionSummary unchanged, so they exercise
    // exactly the same summary logic under test.
    @Inject PopularPDFAccountService popular;
    @Inject BHDPDFAccountService bhd;
    @Inject ScotiabankAccountService scotia;
    @Inject DeductionRepository deductions;

    private static Transaction tx(String desc, float amount) {
        return new Transaction("2024-01-01", "", amount, 0, "", desc);
    }

    // ---------------------------------------------------------------- Popular

    @Test
    void popularCommissionsMatchesEveryHardcodedKeyword() {
        var txns = List.of(
                tx("PAGO SOBREGIRO", 1f),
                tx("CARGO POR SERVICIO mensual", 2f),   // contains "CARGO POR SERV"
                tx("CARGO EMISION tarjeta", 4f),
                tx("PERDIDA varios", 8f),
                tx("COMISIONES generales", 16f),
                tx("SEGURO DEUDOR plan", 32f),
                tx("COM. AVANCE efectivo", 64f),
                tx("COMPRA EN SUPERMERCADO", 1000f));        // no keyword -> excluded

        var s = popular.getTransactionSummary(txns, DeductionType.COMMISSIONS);

        assertEquals(127f, s.total(), 0.001f);
        assertEquals(7, s.transactionsDesc().size());
        assertFalse(s.transactionsDesc().contains("COMPRA EN SUPERMERCADO"));
    }

    @Test
    void popularTaxesInterestAndMora() {
        assertEquals(5f, popular.getTransactionSummary(
                List.of(tx("IMPUESTO selectivo", 5f), tx("x", 9f)), DeductionType.TAXES).total(), 0.001f);
        assertEquals(7f, popular.getTransactionSummary(
                List.of(tx("Intereses por financiamiento", 7f)), DeductionType.INTEREST).total(), 0.001f);
        assertEquals(3f, popular.getTransactionSummary(
                List.of(tx("CARGO POR MORA", 3f)), DeductionType.NON_PAYMENT_FEE).total(), 0.001f);
    }

    @Test
    void userInterestWithoutRulesYieldsZero() {
        var s = popular.getTransactionSummary(
                List.of(tx("cualquier cosa", 10f)), DeductionType.USER_INTEREST);
        assertEquals(0f, s.total(), 0.001f);
        assertTrue(s.transactionsDesc().isEmpty());
    }

    @Test
    @TestTransaction
    void popularUnionsHardcodedKeywordsWithDbRules() {
        deductions.persist(Deduction.build(DeductionType.COMMISSIONS, "NETFLIX"));

        var s = popular.getTransactionSummary(
                List.of(tx("PAGO NETFLIX", 9f), tx("PAGO SOBREGIRO", 1f)),
                DeductionType.COMMISSIONS);

        assertEquals(10f, s.total(), 0.001f);   // db rule (9) + hardcoded SOBREGIRO (1)
    }

    @Test
    @TestTransaction
    void userInterestOutTotalAppendsAmountToDescription() {
        deductions.persist(Deduction.build(DeductionType.USER_INTEREST_OUT_TOTAL, "ABONO"));

        var s = popular.getTransactionSummary(
                List.of(tx("ABONO INTERES", 50f)), DeductionType.USER_INTEREST_OUT_TOTAL);

        assertEquals(50f, s.total(), 0.001f);
        assertTrue(s.transactionsDesc().contains("ABONO INTERES ::  50.0"),
                "OUT_TOTAL must append ' ::  <amount>' to each description, got " + s.transactionsDesc());
    }

    // -------------------------------------------------------------------- BHD

    @Test
    void bhdCommissionsKeywords() {
        var txns = List.of(
                tx("Com. manejo cuenta", 2f),
                tx("PARCIAL ANUAL EMISION", 3f),
                tx("CARGO COBERTURA DE SEGURO", 4f),
                tx("PAGO NORMAL", 100f));

        var s = bhd.getTransactionSummary(txns, DeductionType.COMMISSIONS);
        assertEquals(9f, s.total(), 0.001f);
    }

    @Test
    void bhdTaxesInterestAndMora() {
        assertEquals(2f, bhd.getTransactionSummary(
                List.of(tx("Reten.ley 80-21", 2f)), DeductionType.TAXES).total(), 0.001f);
        assertEquals(6f, bhd.getTransactionSummary(
                List.of(tx("Interes corriente", 6f)), DeductionType.INTEREST).total(), 0.001f);
        assertEquals(4f, bhd.getTransactionSummary(
                List.of(tx("MORA acumulada", 4f)), DeductionType.NON_PAYMENT_FEE).total(), 0.001f);
    }

    // ------------------------------------------------------------- Scotiabank

    @Test
    void scotiaKeywords() {
        assertEquals(5f, scotia.getTransactionSummary(
                List.of(tx("Com. avance", 5f)), DeductionType.COMMISSIONS).total(), 0.001f);
        assertEquals(2f, scotia.getTransactionSummary(
                List.of(tx("Reten.ley", 2f)), DeductionType.TAXES).total(), 0.001f);
        assertEquals(3f, scotia.getTransactionSummary(
                List.of(tx("Interes mensual", 3f)), DeductionType.INTEREST).total(), 0.001f);
        assertEquals(4f, scotia.getTransactionSummary(
                List.of(tx("MORA", 4f)), DeductionType.NON_PAYMENT_FEE).total(), 0.001f);
    }

    @Test
    void emptyTransactionsAlwaysSummariseToZero() {
        for (DeductionType t : DeductionType.values()) {
            assertEquals(0f, popular.getTransactionSummary(List.of(), t).total(), 0.001f, "Popular " + t);
            assertEquals(0f, bhd.getTransactionSummary(List.of(), t).total(), 0.001f, "BHD " + t);
            assertEquals(0f, scotia.getTransactionSummary(List.of(), t).total(), 0.001f, "Scotia " + t);
        }
    }
}
