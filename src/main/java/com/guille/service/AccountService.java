package com.guille.service;

import com.guille.domain.Transaction;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import javax.enterprise.context.ApplicationScoped;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class AccountService {


    public List<Transaction> readPopularCSV(String filePath) throws IOException, CsvValidationException {

        var reader = new CSVReader(new FileReader(filePath));
        var transactions = new ArrayList<Transaction>();
        for(int i=0;i<11;i++)
            reader.readNext();

        // read line by line
        String[] record = null;
        while ((record = reader.readNext()) != null) {
            if("".equals(record[1].trim()))
                break;
            var t = new Transaction(record[0],
                    record[1],
                    Float.parseFloat(record[2]),
                    Integer.parseInt(record[3]),
                    record[4],
                    record[5]);
            transactions.add(t);
//            System.out.println(Arrays.toString(record));
            System.out.println(t);
        }

        reader.close();
        return transactions;
    }
}
