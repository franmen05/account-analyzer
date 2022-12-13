package com.guille.service.bank;

import com.guille.domain.Transaction;
import com.guille.domain.TransactionSummary;
import com.guille.domain.TransactionType;
import com.guille.service.AccountService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@ApplicationScoped
public class PopularPDFAccountService implements AccountService {

    public static String NAME = PopularPDFAccountService.class.getSimpleName();

    int count = 1;
    public List<Transaction> readFile(Path filePath,String... additionalParam) throws IOException {
        PDDocument document = null;
        var transactions = new ArrayList<Transaction>();
        try {
            document = PDDocument.load(filePath.toFile(), additionalParam[0]);
            var pdfStripper = new PDFTextStripper();
            pdfStripper.setEndPage(document.getNumberOfPages());

            var pages = pdfStripper.getText(document);
            var lines = pages.split("\\r\\n|\\r|\\n");
            var rows= Arrays.stream(lines).distinct().toArray();
            Arrays.stream(rows).forEach(s -> System.out.println(count++ +" : "+s));

            count=1;

            var line = new StringBuilder();

            List<String> record=new ArrayList<>();
            for (int i = 9;i< rows.length-(3*4);i++) {

                String temp=lines[i];
                if(count==3){

                    count=0;
                    record.add(temp);
                    System.out.println(record);
                    try {

                        var t = new Transaction(record.get(1),
                                "",
                                Float.parseFloat(record.get(5).isBlank() ? "0" : record.get(5).replace(",","")),
                                Integer.parseInt(record.get(4).isBlank() ? "0" : record.get(4).replace(",","")),
                                record.get(2),
                                record.get(3));

                        transactions.add(t);
                    } catch (Exception ignored) {}

                    record.clear();
                }else if(count==1){

                    record.addAll(Arrays.stream(temp.split(" ",4)).toList());
                }else if(count==2){

                    if(temp.isBlank())
                        record.add("0");
                    else
                        record.add(temp.replaceAll("\\s+"," ").split(" ")[1]);
                }else{
                    record.add(temp);
                }
                count++;
            }
        } catch (IOException e) {
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

        transactions.forEach(System.out::println);
        return transactions;
    }


    public TransactionSummary getTransactionSummary(List<Transaction> transactions, TransactionType type) {

        var transactionDesList= new HashSet<String>();
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
