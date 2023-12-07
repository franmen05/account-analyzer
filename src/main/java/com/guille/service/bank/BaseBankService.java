package com.guille.service.bank;

import com.guille.domain.Deduction;
import com.guille.domain.DeductionType;
import com.guille.domain.Transaction;
import com.guille.domain.TransactionSummary;
import com.guille.reposiitory.DeductionRepository;
import com.guille.service.AccountService;
import jakarta.inject.Inject;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BaseBankService implements AccountService {

    @Inject
    DeductionRepository deductionRepository;

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

    protected TransactionSummary buildTransactionSummary(List<Transaction> transactions, DeductionType deductionType) {
        final var dedution = deductionRepository.find("type", deductionType)
                .stream().map(Deduction::getDescription)
                .collect(Collectors.toSet());

        return buildTransactionSummary(transactions.stream()
                .filter(
                        account -> account.descContains(dedution)
                ));
    }


}
