package com.guille.service;

import com.guille.domain.Transaction;
import com.guille.domain.TransactionSummary;
import com.guille.domain.TransactionType;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import javax.enterprise.context.ApplicationScoped;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;


public interface AccountService {

     List<Transaction> readCSV(String filePath) throws IOException, CsvValidationException;
    TransactionSummary getTransactionSummary(List<Transaction> transactions, TransactionType type);
}
