package com.guille.service.bank;

import com.guille.domain.Deduction;
import com.guille.domain.DeductionType;
import com.guille.domain.Transaction;
import com.guille.domain.TransactionSummary;
import com.guille.reposiitory.DeductionRepository;
import com.guille.service.AccountService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class ScotiabankAccountService implements AccountService {

    private int count = 1;
    @Inject
    DeductionRepository deductionRepository;




    public List<Transaction> readFile(Path filePath, String... additionalParam) {

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

//            var line = new StringBuilder();

            List<String> record=new ArrayList<>();
            for (int i = 9;i< rows.length-(3*4);i++) {

                var temp=lines[i];
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


//    public static String NAME= ScotiabankAccountService.class.getSimpleName();

//    public List<Transaction> readFile(Path filePath,String... additionalParam) throws IOException {
//
//        System.out.println("<=> XD <::> " + filePath);
//        var fileInputStream = new FileInputStream(filePath.toFile());
//        var transactions = new ArrayList<Transaction>();
//
//        try {
//            var workbook = WorkbookFactory.create(fileInputStream);
//
//            var sheet = workbook.getSheetAt(0);
//
//            for (Row row : sheet) {
//
//                System.out.print(row.getRowNum() + " :: ");
//                System.out.println(row.getCell(3));
//
//                if (row.getCell(0).getStringCellValue().contains("Fecha")
//                        || row.getCell(0).getStringCellValue().trim().equals(""))
//                    continue;
//
//                var t = new Transaction(row.getCell(0).getStringCellValue(),
//                        "",
//                        Float.parseFloat(getNumberFromString(row, 3)),
//                        Integer.parseInt(getNumberFromString(row, 1)),
//                        "",
//                        row.getCell(2).getStringCellValue());
//
//                transactions.add(t);
//            }
//            workbook.close();
//            fileInputStream.close();
//
//        }catch (Exception e){
//            fileInputStream.close();
//            throw  e;
//        }
//
//        return transactions;
//    }

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

//        }else if(type==TransactionType.NON_PAYMENT_FEE) {
        } {
            return buildTransactionSummary( transactions.stream()
                    .filter(
                            account -> account.descContains("MORA")
                                    || account.descContains(deductionRepository.find("type",DeductionType.NON_PAYMENT_FEE)
                                    .stream().map(Deduction::getDescription)
                                    .collect(Collectors.toSet()))
                    ));
        }
    }

    private TransactionSummary buildTransactionSummary(Stream<Transaction> transactionStream) {

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
