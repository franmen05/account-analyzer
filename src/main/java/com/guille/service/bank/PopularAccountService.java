package com.guille.service.bank;

import com.guille.domain.Deduction;
import com.guille.domain.DeductionType;
import com.guille.domain.Transaction;
import com.guille.domain.TransactionSummary;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class PopularAccountService extends BaseBankService {

    public List<Transaction> readFile(Path filePath,String... additionalParam) throws IOException {

        var reader = new CSVReader(new FileReader(filePath.toFile()));
        try {
//        var reader = new CSVReader(new FileReader(constants.uploadDir()+"/"+fileName));
            var transactions = new ArrayList<Transaction>();
            for (int i = 0; i < 11; i++)
                reader.readNext();

            // read line by line
            String[] record;
            while ((record = reader.readNext()) != null) {
                System.out.print(record.length + " :: ");
                System.out.println(Arrays.toString(record));
                if (record.length <= 1)
                    continue;

                if (record[0].trim().contains("Fecha")
                        || record[0].trim().equals(""))
                    continue;
                try {

                    var t = new Transaction(record[0],
                            record[1],
                            Float.parseFloat(record[2].isBlank() ? "0" : record[2]),
                            Integer.parseInt(record[3].isBlank() ? "0" : record[3]),
                            record[4],
                            record[5]);

                    transactions.add(t);
                } catch (ArrayIndexOutOfBoundsException ignored) {
                }

//            System.out.println(t);
            }


            reader.close();
            return transactions;
        }catch (CsvValidationException e){
            reader.close();
            var ex=new IOException(e.getMessage());
            ex.setStackTrace(e.getStackTrace());
            throw ex;
        }
    }


    public TransactionSummary getTransactionSummary(List<Transaction> transactions, DeductionType type) {

        var transactionDesList= new HashSet<String>();
        var total = 0f;

        if(type== DeductionType.COMMISSIONS) {

            total = transactions.stream()
                    .filter(
                        account -> account.descContains("SOBREGIRO")
//                            || account.descContains("CARGO POR SERVICIO")
                            || account.descContains("CARGO POR SERV")
                            || account.descContains("CARGO EMISION")
                            || account.descContains("PERDIDA")
                            || account.descContains("COMISIONES")
                            || account.descContains("SEGURO DEUDOR")
                            || account.descContains("COM. AVANCE")
                            || account.descContains(deductionRepository.find("type",DeductionType.COMMISSIONS)
                                .stream().map(Deduction::getDescription)
                                .collect(Collectors.toSet()))
                    )
                    .map(transaction -> getAmount(transaction, transactionDesList))
                    .reduce(0.0f, Float::sum);

        }else if(type== DeductionType.TAXES) {
            total = transactions.stream()
                    .filter(
                            account -> account.descContains("IMPUESTO")
                                    || account.descContains(deductionRepository.find("type",DeductionType.TAXES)
                                    .stream().map(Deduction::getDescription)
                                    .collect(Collectors.toSet()))
                    )
                    .map(transaction -> getAmount(transaction, transactionDesList))
                    .reduce(0.0f, Float::sum);

        }else if(type== DeductionType.INTEREST) {
            total = transactions.stream()
                    .filter(
                            account -> account.descContains("Interes")
                                    || account.descContains(deductionRepository.find("type",DeductionType.INTEREST)
                                    .stream().map(Deduction::getDescription)
                                    .collect(Collectors.toSet()))
                    )
                    .map(transaction ->  getAmount(transaction, transactionDesList))
                    .reduce(0.0f, Float::sum);

        }else if(type== DeductionType.NON_PAYMENT_FEE) {
            total = transactions.stream()
                    .filter(
                            account -> account.descContains("MORA")
                                    || account.descContains(deductionRepository.find("type",DeductionType.NON_PAYMENT_FEE)
                                        .stream().map(Deduction::getDescription)
                                        .collect(Collectors.toSet()))
                    )
                    .map(transaction -> getAmount(transaction, transactionDesList))
                    .reduce(0.0f, Float::sum);
//
//        }else if(type==DeductionType.USER_INTEREST) {
//            return buildTransactionSummary(transactions.stream()
//                    .filter(
//                            account -> account.descContains(deductionRepository.find("type", DeductionType.USER_INTEREST)
//                                    .stream().map(Deduction::getDescription)
//                                    .collect(Collectors.toSet()))
//                    ));
//        }else if(type==DeductionType.USER_INTEREST_OUT_TOTAL) {
//            return buildTransactionSummary( transactions.stream()
//                .filter(
//                        account ->  account.descContains(deductionRepository.find("type",DeductionType.USER_INTEREST_OUT_TOTAL)
//                                .stream().map(Deduction::getDescription)
//                                .collect(Collectors.toSet()))
//                ));
//        }
        }else{
            return buildTransactionSummary(transactions, type);
        }
        return new TransactionSummary(transactionDesList,total);
    }

    private static Float getAmount(Transaction t, Set<String> transactionDesList) {
        transactionDesList.add(t.desc());
        return t.amount();
    }
}
