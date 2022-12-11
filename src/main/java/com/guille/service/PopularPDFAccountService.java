package com.guille.service;

import com.guille.domain.Transaction;
import com.guille.domain.TransactionSummary;
import com.guille.domain.TransactionType;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.enterprise.context.ApplicationScoped;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@ApplicationScoped
public class PopularPDFAccountService implements AccountService {

    public static String NAME = PopularPDFAccountService.class.getSimpleName();

//    @Inject
//    Constants constants;

    public List<Transaction> readFile(Path filePath) throws IOException {
        PDDocument document = null;
        try {
            document = PDDocument.load(filePath.toFile(), "22300843194");
            var pdfStripper = new PDFTextStripper();
//            pdfStripper.setStartPage(1);
            pdfStripper.setEndPage(document.getNumberOfPages());

            String pages = pdfStripper.getText(document);
//            System.out.println(pages);
            //split by detecting newline
            var lines = pages.split("\\r\\n|\\r|\\n");

            int count = 1;   //Just to indicate line number
            ;   //Just to indicate line number

            String  line = "";
            for (int countl = 9;countl<=lines.length;countl++) {
                String temp=lines[countl];
//                System.out.println(countl + " : " + temp);
                if(count==3){
                    count=0;
                    line=line + temp;
                    System.out.println(countl + " : " + line);
                    line="";
                }{
                    line=line + temp;
                }
                count++;
            }
/*
            var transactions = new ArrayList<Transaction>();
            Arrays.stream(lines).filter(s -> s.equalsIgnoreCase());

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

*/

//            System.out.println(pages);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (document != null) {
                try {
                    document.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }


//        try {
////        var reader = new CSVReader(new FileReader(constants.uploadDir()+"/"+fileName));
//            var transactions = new ArrayList<Transaction>();
//            for (int i = 0; i < 11; i++)
//                reader.readNext();
//
//            // read line by line
//            String[] record;
//            while ((record = reader.readNext()) != null) {
//                System.out.print(record.length + " :: ");
//                System.out.println(Arrays.toString(record));
//                if (record.length <= 1)
//                    continue;
//
//                if (record[0].trim().contains("Fecha")
//                        || record[0].trim().equals(""))
//                    continue;
//                try {
//
//                    var t = new Transaction(record[0],
//                            record[1],
//                            Float.parseFloat(record[2].isBlank() ? "0" : record[2]),
//                            Integer.parseInt(record[3].isBlank() ? "0" : record[3]),
//                            record[4],
//                            record[5]);
//
//                    transactions.add(t);
//                } catch (ArrayIndexOutOfBoundsException ignored) {
//                }
//
////            System.out.println(t);
//            }
//
//
//            reader.close();
//            return transactions;
//        }catch (CsvValidationException e){
//            reader.close();
//            var ex=new IOException(e.getMessage());
//            ex.setStackTrace(e.getStackTrace());
//            throw ex;
//        }
        return null;
    }


    public TransactionSummary getTransactionSummary(List<Transaction> transactions, TransactionType type) {

        var  transactionDesList= new HashSet<String>();
        var total = 0f;

        if(type==TransactionType.COMMISSIONS) {

            total = transactions.stream()
                    .filter(
                            account -> account.descContains("SOBREGIRO")
                                    || account.descContains("CARGO POR SERVICIO")
                                    || account.descContains("CARGO POR SERV")
                                    || account.descContains("CARGO EMISION")
                                    || account.descContains("PERDIDA")
                                    || account.descContains("COMISIONES")
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
