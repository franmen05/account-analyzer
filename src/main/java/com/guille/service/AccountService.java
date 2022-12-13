package com.guille.service;

import com.guille.domain.Transaction;
import com.guille.domain.TransactionSummary;
import com.guille.domain.TransactionType;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;


public interface AccountService {

     List<Transaction> readFile(Path filePath,String... additionalParam) throws IOException;
    TransactionSummary getTransactionSummary(List<Transaction> transactions, TransactionType type);
}
