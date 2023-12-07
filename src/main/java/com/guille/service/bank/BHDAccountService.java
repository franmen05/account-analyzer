package com.guille.service.bank;

import com.guille.domain.Deduction;
import com.guille.domain.DeductionType;
import com.guille.domain.Transaction;
import com.guille.domain.TransactionSummary;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class BHDAccountService extends BaseBankService {


    public List<Transaction> readFile(Path filePath,String... additionalParam) throws IOException {

        System.out.println("<=> XD <::> " + filePath);
        var fileInputStream = new FileInputStream(filePath.toFile());
        var transactions = new ArrayList<Transaction>();

        try {
            var workbook = WorkbookFactory.create(fileInputStream);

            var sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {

                System.out.print(row.getRowNum() + " :: ");
                System.out.println(row.getCell(3));

                if (row.getCell(0).getStringCellValue().contains("Fecha")
                        || row.getCell(0).getStringCellValue().trim().isEmpty())
                    continue;

                var t = new Transaction(row.getCell(0).getStringCellValue(),
                        "",
                        Float.parseFloat(getNumberFromString(row, 3)),
                        Integer.parseInt(getNumberFromString(row, 1)),
                        "",
                        row.getCell(2).getStringCellValue());

                transactions.add(t);
            }
            workbook.close();
            fileInputStream.close();

        }catch (Exception e){
            fileInputStream.close();
            throw  e;
        }

        return transactions;
    }

    protected static String getNumberFromString(Row row, int i) {
        var val = row.getCell(i).toString();
        return val.isBlank()?"0":val;
    }

    public TransactionSummary getTransactionSummary(List<Transaction> transactions, DeductionType type) {

        if(type== DeductionType.COMMISSIONS) {

            return buildTransactionSummary(transactions.stream()
                    .filter(
                            account -> account.descContains("Com. ")
                            || account.descContains("PARCIAL ANUAL EMISION")
                            || account.descContains("CARGO COBERTURA DE SEGURO")
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

        }
        else if(type==DeductionType.NON_PAYMENT_FEE) {
//        } {
            return buildTransactionSummary( transactions.stream()
                    .filter(
                            account -> account.descContains("MORA")
                                    || account.descContains(deductionRepository.find("type",DeductionType.NON_PAYMENT_FEE)
                                        .stream().map(Deduction::getDescription)
                                        .collect(Collectors.toSet()))
                    ));
        }
//        else if(type==DeductionType.USER_INTEREST_OUT_TOTAL) {
////        } {
//            return getTransactionSummary2(transactions, DeductionType.USER_INTEREST_OUT_TOTAL);
//        }{
//        else if(type==DeductionType.USER_INTEREST) {
            return buildTransactionSummary(transactions, type);
//        }
//        return new TransactionSummary("",0f);
    }

}
