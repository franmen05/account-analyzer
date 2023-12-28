package com.guille.service.bank;

import com.guille.domain.Transaction;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class PopularPDFAccountService extends PopularAccountService {

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
            System.out.println(Arrays.toString(lines));
            var rows= Arrays.stream(lines).distinct().toArray();
            System.out.println(Arrays.toString(rows));

            List<String> record=new ArrayList<>();


            for (int i = 9;i< lines.length-12;i++) {

                var temp=lines[i];
                if(count==3){

                    count=0;
                    record.add(temp);
                    System.out.println(i+" || "+record);
                    try {

                        var t = new Transaction(record.get(1),
                                "",
                                Float.parseFloat(record.get(5).isBlank() ? "0" : record.get(5).replace(",","")),
                                Integer.parseInt(record.get(4).isBlank() ? "0" : record.get(4).replace(",","")),
                                record.get(2),
                                record.get(3)
                        );

                        transactions.add(t);
                    } catch (Exception ignored) {}

                    record.clear();
                }else if(count==1){

                    record.addAll(Arrays.stream(temp.split(" ",4)).toList());
                }else if(count==2){

                    if(temp.isBlank())
                        record.add("0");
                    else {
                        String[] split = temp.replaceAll("\\s+", " ").split(" ");
                        record.add(split.length>1? split[1] :"");
                    }
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
}
