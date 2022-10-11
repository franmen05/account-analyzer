package com.guille.service;

import com.guille.domain.Transaction;
import com.guille.domain.TransactionSummary;
import com.guille.domain.TransactionType;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

@ApplicationScoped
public class BHDAccountService implements AccountService{


    public static String NAME=BHDAccountService.class.getSimpleName();

    public List<Transaction> readFile(Path filePath) throws IOException {

        System.out.println("=> XD :: " + filePath);

        var fileInputStream = new FileInputStream(filePath.toFile());
        var workbook = WorkbookFactory.create(fileInputStream);
        var transactions = new ArrayList<Transaction>();
        try {

            var sheet = workbook.getSheetAt(0);


            for (Row row : sheet) {

                System.out.print(row.getRowNum() + " :: ");
                System.out.println(row.getCell(3));
//            System.out.println(Arrays.toString(record));
//            if(record.length<=1 )
//                continue;

                if (row.getCell(0).getStringCellValue().contains("Fecha")
                        || row.getCell(0).getStringCellValue().trim().equals(""))
                    continue;

                var t = new Transaction(row.getCell(0).getStringCellValue(),
                        "",
                        Float.parseFloat(getString(row, 3)),
                        Integer.parseInt(getString(row, 1)),
                        "",
                        row.getCell(2).getStringCellValue());

                transactions.add(t);
            }
            workbook.close();
            fileInputStream.close();
        }catch (Exception e){
            workbook.close();
            fileInputStream.close();
            throw  e;
        }

        return transactions;
    }

    private static String getString(Row row, int i) {
        var val = row.getCell(i).toString();
        return val.isBlank()?"0":val;
    }



    public TransactionSummary getTransactionSummary(List<Transaction> transactions, TransactionType type) {



        if(type==TransactionType.COMMISSIONS) {

            return getTotal(transactions.stream()
                    .filter(
                            account -> account.descContains("Com. ")
                    ));

        }else if(type==TransactionType.TAXES) {
            return getTotal( transactions.stream()
                    .filter(
                            account -> account.descContains("Reten.ley")
                    ));

        }else if(type==TransactionType.INTEREST) {
            return getTotal(transactions.stream()
                    .filter(
                            account -> account.descContains("Interes")
                    ));

//        }else if(type==TransactionType.NON_PAYMENT_FEE) {
        } {
            return getTotal( transactions.stream()
                    .filter(
                            account -> account.descContains("MORA")
                    ));
        }
//        return new TransactionSummary("",0f);
    }

    private TransactionSummary getTotal( Stream<Transaction> transactionStream) {

        var  transactionSet= new HashSet<String>();
        var total = 0f;

        total = transactionStream.map(t -> {
                    transactionSet.add(t.desc());
                    return t.amount();
                })
                .reduce(0.0f, Float::sum);

//        return total;
        return new TransactionSummary(transactionSet,total);
    }

}
