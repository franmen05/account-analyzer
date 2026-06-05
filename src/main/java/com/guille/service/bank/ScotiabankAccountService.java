package com.guille.service.bank;

import com.guille.domain.DeductionType;
import com.guille.domain.Transaction;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
            Arrays.stream(rows).forEach(s -> Log.debugf("%d : %s", count++, s));

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

                    Log.debugf("record=%s", record);
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
            Log.error("Failed to read Scotiabank PDF", e);
        } finally {
            if (document != null) {
                try {
                    document.close();
                } catch (IOException e) {
                    Log.warn("Could not close PDF document", e);
                }
            }
        }

        transactions.forEach(t -> Log.debug(t));
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
    @Override
    protected Map<DeductionType, Set<String>> keywordsByType() {
        return Map.of(
                DeductionType.COMMISSIONS, Set.of("Com. "),
                DeductionType.TAXES, Set.of("Reten.ley"),
                DeductionType.INTEREST, Set.of("Interes"),
                DeductionType.NON_PAYMENT_FEE, Set.of("MORA"));
    }

}
