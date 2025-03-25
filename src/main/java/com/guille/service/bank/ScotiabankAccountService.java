package com.guille.service.bank;

import com.guille.domain.Deduction;
import com.guille.domain.DeductionType;
import com.guille.domain.Transaction;
import com.guille.domain.TransactionSummary;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Row;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@ApplicationScoped
public class ScotiabankAccountService extends BaseBankService {

    private int count = 1;

    public List<Transaction> readFile(Path filePath, String... additionalParam) {

        PDDocument document = null;
        var transactions = new ArrayList<Transaction>();

        try {
            document = Loader.loadPDF(filePath.toFile(), additionalParam[0]);
            var pdfStripper = new PDFTextStripper();
            pdfStripper.setEndPage(document.getNumberOfPages());

            var pages = pdfStripper.getText(document);
            var lines = pages.split("\\r\\n|\\r|\\n");
            var rows= Arrays.stream(lines).distinct().toArray();
            Arrays.stream(rows).forEach(s -> System.out.println(count++ +" : "+s));

            List<String> record=new ArrayList<>();
            boolean usdt=false;
            for (int i = 94;i< rows.length-20;i++) {

                var temp=rows[i].toString();
                if(temp.isBlank())
                    continue;


                if (temp.contains("USD"))
                    usdt=true;

                    List<String> list = Arrays.stream(temp.split(" ", 4)).toList();

                    if(list.size()<4)
                        continue;


                    String amount = Arrays.stream(list.get(3).split("[^\\d.]+"))
                        .filter(s -> !s.isBlank())
                         .findFirst()
                        .map(List::of)
                        .orElseGet(() -> List.of("0"))
                        .getFirst();

                    amount = amount.substring(0,amount.length()-1);

                    record.addAll(list);
//                    if(extractDates(record.get(1)).isEmpty())
//                        continue;

                    System.out.println(record);
                    try {

                        String referenceNum = record.get(0).substring(0,record.get(0).length()-1);
                        var t = new Transaction(record.get(1),
                                "",
                                Float.parseFloat(amount.isBlank() ? "0" : amount.replace(",","")),
                                Integer.parseInt(referenceNum.isBlank() ? "0" : referenceNum.replace(",","")),
                                "",
                                usdt?"USD "+record.get(3):record.get(3));

                        transactions.add(t);
                    } catch (Exception ignored) {
//                        System.out.println(ignored.getMessage());
                    }

                    record.clear();

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


    public static List<String> extractDates(String text) {
        String regex = "\\b(\\d{4}-\\d{2}-\\d{2}|\\d{2}/\\d{2}/\\d{4}|\\d{2}-[A-Za-z]{3}-\\d{4})\\b";

        return Pattern.compile(regex)
                .matcher(text)
                .results()
                .map(match -> match.group())
                .collect(Collectors.toList());
    }
    private static String getNumberFromString(Row row, int i) {
        var val = row.getCell(i).toString();
        return val.isBlank()?"0":val;
    }

    public TransactionSummary getTransactionSummary(List<Transaction> transactions, DeductionType type) {

        if(type== DeductionType.COMMISSIONS) {

            return buildTransactionSummary(transactions.stream()
                    .filter(
                            account -> account.descContains("Com. ")
                                    || account.descContains(deductionRepository.find("type",DeductionType.COMMISSIONS)
                                        .stream().map(Deduction::getDescription)
                                        .collect(Collectors.toSet()))
                    ));

        }else if(type== DeductionType.TAXES) {
            return buildTransactionSummary( transactions.stream()
                    .filter(
                            account -> account.descContains("Reten.ley")
                                    || account.descContains(deductionRepository.find("type",DeductionType.TAXES)
                                        .stream().map(Deduction::getDescription)
                                        .collect(Collectors.toSet()))
                    ));

        }else if(type== DeductionType.INTEREST) {
            return buildTransactionSummary(transactions.stream()
                    .filter(
                            account -> account.descContains("Interes")
                                    || account.descContains(deductionRepository.find("type",DeductionType.INTEREST)
                                        .stream().map(Deduction::getDescription)
                                        .collect(Collectors.toSet()))
                    ));

        }else if(type==DeductionType.NON_PAYMENT_FEE) {
//        } {
            return buildTransactionSummary(transactions.stream()
                    .filter(
                            account -> account.descContains("MORA")
                                    || account.descContains(deductionRepository.find("type", DeductionType.NON_PAYMENT_FEE)
                                    .stream().map(Deduction::getDescription)
                                    .collect(Collectors.toSet()))
                    ));
//        }else if(type==DeductionType.USER_INTEREST) {
        }else{
            return buildTransactionSummary(transactions, type);
        }
    }

}
