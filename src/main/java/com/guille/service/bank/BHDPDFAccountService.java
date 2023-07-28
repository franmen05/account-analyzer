package com.guille.service.bank;

import com.guille.domain.Transaction;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class BHDPDFAccountService extends  BHDAccountService {


    public static String NAME= BHDPDFAccountService.class.getSimpleName();
    private final int count = 1;

    public List<Transaction> readFile(Path filePath, String... additionalParam) {
        PDDocument document = null;
        var transactions = new ArrayList<Transaction>();
        try {
            document = PDDocument.load(filePath.toFile(), additionalParam[0]);
            var pdfStripper = new PDFTextStripper();
            pdfStripper.setEndPage(document.getNumberOfPages());

            var pages = pdfStripper.getText(document);
            var lines = pages.split("\\r\\n|\\r|\\n");
//            var rows= Arrays.stream(lines).distinct().toArray();
            List<String> rows= Arrays.stream(lines).toList();
//            rows.forEach(s -> System.out.println(count++ +" : "+s));


            var startIndex=rows.indexOf(rows.stream().filter(s -> s.contains("TRANSACCIONES EN PESOS RD")).findFirst().orElse(""));
            var endIndex=rows.indexOf(rows.stream().filter(s -> s.contains("TOTAL TRANSACCIONES EN RD$ PESOS")).findFirst().orElse(""));
//            System.out.println(startIndex);
//            System.out.println(endIndex);
//            count=1;

//            var line = new StringBuilder();

            List<String> colunm=new ArrayList<>();

            for (int i = startIndex+1;i< endIndex;i++) {

                var row=rows.get(i);
                if(row.contains("DEBITO")) continue;

//                System.out.println(Arrays.toString(row.split(" ",4)));

                String[] split = row.split(" ");
                var desc="";
                for (int j = 0; j < split.length; j++) {
                    if(j==2 && !isNumeric(split[j])) {
                        colunm.add("00");
                        desc=" "+split[j];
                    }else if(j<3)
                        colunm.add(split[j]);
                    else if((split.length -1 )==j)
                        colunm.add(split[j]);
                    else{
                        desc+=" "+split[j];
                    }
                }
                colunm.add(desc);

//                if(count==3){
//
//                    count=0;
//                    colunm.add(row);
//                    System.out.println(colunm);


                    try {

                        var t = new Transaction(colunm.get(1),
                                "",
                                Float.parseFloat(colunm.get(3).isBlank() ? "0" : colunm.get(3).replace(",","")),
                                Integer.parseInt(colunm.get(2).isBlank() ? "0" : colunm.get(2).replace(",","")),
                                "",
                                colunm.get(4));

                        transactions.add(t);
                    } catch (Exception ignored) {ignored.printStackTrace();}

                    colunm.clear();
//                }else if(count==1){
//
//                    record.addAll(Arrays.stream(temp.split(" ",4)).toList());
//                }else if(count==2){
//
//                    if(temp.isBlank())
//                        record.add("0");
//                    else
//                        record.add(temp.replaceAll("\\s+"," ").split(" ")[1]);
//                }else{
//                    record.add(temp);
//                }
//                count++;
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

    public static boolean isNumeric(String strNum) {
        if (strNum == null)
            return false;

        try {
            Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

}
