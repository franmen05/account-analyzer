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

@ApplicationScoped
public class BHDAccountService implements AccountService{


    public static String NAME=BHDAccountService.class.getSimpleName();

//    @Inject
//    Constants constants;

    public List<Transaction> readCSV(String filePath) throws IOException, CsvValidationException {

//        var reader = new CSVReader(new FileReader(constants.uploadDir()+"/"+fileName));
        var reader = new CSVReader(new FileReader(filePath));
        var transactions = new ArrayList<Transaction>();
//        for(int i=0;i<11;i++)
        reader.readNext();

        // read line by line
        String[] record;
        while ((record = reader.readNext()) != null) {
            System.out.println(record[3].isBlank());
            System.out.print(record.length+" :: ");
            System.out.println(Arrays.toString(record));
            if(record.length<=1 )
                continue;

            if(record[0].trim().contains("Fecha")
                    || record[0].trim().equals("") )
                continue;
            try{

                var t = new Transaction(record[0],
                    "",
                    Float.parseFloat(record[3].isBlank() ? "0":record[3].replace("RD$","").replace(",","")),
                    Integer.parseInt(record[1].isBlank() ? "0":record[1].replace("RD$","").replace(",","")),
                    "",
                    record[2]);

                transactions.add(t);
            }catch (ArrayIndexOutOfBoundsException ignored){}

//            System.out.println(t);
        }

        reader.close();
        return transactions;
    }


    public TransactionSummary getTransactionSummary(List<Transaction> transactions, TransactionType type) {

        var  transactionDesList= new HashSet<String>();
        var total = 0f;

        if(type==TransactionType.COMMISSIONS) {

            total = transactions.stream()
                    .filter(
                            account -> account.descContains("Com. ")
                    )
                    .map(t -> {
                        transactionDesList.add(t.desc());
                        return t.amount();
                    })
                    .reduce(0.0f, Float::sum);

        }else if(type==TransactionType.TAXES) {
            total = transactions.stream()
                    .filter(
                            account -> account.descContains("IMPUESTO")
                    )
                    .map(t -> {
                        transactionDesList.add(t.desc());
                        return t.amount();
                    })
                    .reduce(0.0f, Float::sum);

        }else if(type==TransactionType.INTEREST) {
            total = transactions.stream()
                    .filter(
                            account -> account.descContains("Interes")
                    )
                    .map(t -> {
                        transactionDesList.add(t.desc());
                        return t.amount();
                    })
                    .reduce(0.0f, Float::sum);

        }else if(type==TransactionType.NON_PAYMENT_FEE) {
            total = transactions.stream()
                    .filter(
                            account -> account.descContains("MORA")
                    )
                    .map(t -> {
                        transactionDesList.add(t.desc());
                        return t.amount();
                    })
                    .reduce(0.0f, Float::sum);

        }
        return new TransactionSummary(transactionDesList,total);
    }
}
