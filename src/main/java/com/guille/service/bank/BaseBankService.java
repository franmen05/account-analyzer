package com.guille.service.bank;

import com.guille.domain.Deduction;
import com.guille.domain.DeductionType;
import com.guille.domain.Transaction;
import com.guille.domain.TransactionSummary;
import com.guille.repository.DeductionRepository;
import com.guille.service.AccountService;
import jakarta.inject.Inject;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BaseBankService implements AccountService {

    @Inject
    DeductionRepository deductionRepository;

    /**
     * Hard-coded, bank-specific keywords for each deduction type. A transaction
     * counts toward a type when its description contains any of these keywords
     * OR any description configured as a {@code Deduction} rule for that type.
     * Banks override this; types absent from the map fall back to rule-only
     * matching (see {@link #getTransactionSummary}).
     */
    protected Map<DeductionType, Set<String>> keywordsByType() {
        return Map.of();
    }

    @Override
    public TransactionSummary getTransactionSummary(List<Transaction> transactions, DeductionType type) {
        var keywords = keywordsByType().get(type);
        if (keywords != null)
            return summarizeByKeywords(transactions, type, keywords);
        return buildTransactionSummary(transactions, type);
    }

    /** Union the bank's hard-coded keywords with the configured rules, then summarize matches. */
    private TransactionSummary summarizeByKeywords(List<Transaction> transactions, DeductionType type, Set<String> hardcoded) {
        var keywords = new HashSet<>(hardcoded);
        deductionRepository.find("type", type)
                .stream().map(Deduction::getDescription)
                .forEach(keywords::add);
        return buildTransactionSummary(transactions.stream().filter(t -> t.descContains(keywords)));
    }

    protected TransactionSummary buildTransactionSummary(Stream<Transaction> transactionStream) {

        var  transactionSet= new HashSet<String>();
        var total = 0f;

        total = transactionStream.map(t -> {
                    transactionSet.add(t.desc());
                    return t.amount();
                })
                .reduce(0.0f, Float::sum);

        return new TransactionSummary(transactionSet,total);
    }

    protected TransactionSummary buildTransactionSummary(Stream<Transaction> transactionStream, DeductionType dt) {

        var  transactionSet= new TreeSet<String>();
        var total = 0f;

        total = transactionStream.map(t -> {
                    transactionSet.add(t.desc()+" ::  "+t.amount());
                    return t.amount();
                })
                .reduce(0.0f, Float::sum);

        return new TransactionSummary(transactionSet,total);
    }

    protected TransactionSummary buildTransactionSummary(List<Transaction> transactions, DeductionType deductionType) {
        final var dedution = deductionRepository.find("type", deductionType)
                .stream().map(Deduction::getDescription)
                .collect(Collectors.toSet());
        if (deductionType==DeductionType.USER_INTEREST_OUT_TOTAL)
            return buildTransactionSummary(
                    transactions.stream().filter(account -> account.descContains(dedution)),deductionType);
        else
            return buildTransactionSummary(
                    transactions.stream().filter(account -> account.descContains(dedution)));
    }


}
