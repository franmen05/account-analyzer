package com.guille.service;

import com.guille.config.Constants;
import com.guille.domain.Transaction;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class AccountService {

    @Inject
    Constants constants;

    public List<Transaction> readPopularCSV(String filePath) throws IOException, CsvValidationException {

        var reader = new CSVReader(new FileReader(constants.uploadDir()+"/"+filePath));
        var transactions = new ArrayList<Transaction>();
        for(int i=0;i<11;i++)
            reader.readNext();

        // read line by line
        String[] record = null;
        while ((record = reader.readNext()) != null) {
//            System.out.println(Arrays.toString(record));
//            System.out.println(record.length);
            if(record.length<2 )
                break;
            if(record[0].trim().equalsIgnoreCase("Fecha Posteo")
                    || record[0].trim().equals("") )
                continue;

            var t = new Transaction(record[0],
                    record[1],
                    Float.parseFloat(record[2].isEmpty() ? "0":record[2]),
                    Integer.parseInt(record[3].isEmpty() ? "0":record[3]),
                    record[4],
                    record[5]);
            transactions.add(t);

            System.out.println(t);
        }

        reader.close();
        return transactions;
    }
}
