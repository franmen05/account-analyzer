package com.guille.service.bank;

import com.guille.domain.Transaction;
import com.guille.domain.TransactionSummary;
import com.guille.reposiitory.DeductionRepository;
import com.guille.service.AccountService;

import javax.inject.Inject;
import java.util.HashSet;
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

}
