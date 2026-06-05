package com.guille.service.bank;

import com.guille.domain.DeductionType;
import com.guille.domain.Transaction;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class BHDAccountService extends BaseBankService {


    public List<Transaction> readFile(Path filePath,String... additionalParam) throws IOException {

        Log.debugf("Reading BHD Excel statement: %s", filePath);
        var transactions = new ArrayList<Transaction>();

        try (var fileInputStream = new FileInputStream(filePath.toFile());
             var workbook = WorkbookFactory.create(fileInputStream)) {

            var sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {

                Log.debugf("%d :: %s", row.getRowNum(), row.getCell(3));

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
        }

        return transactions;
    }

    protected static String getNumberFromString(Row row, int i) {
        var val = row.getCell(i).toString();
        return val.isBlank()?"0":val;
    }

    @Override
    protected Map<DeductionType, Set<String>> keywordsByType() {
        return Map.of(
                DeductionType.COMMISSIONS, Set.of(
                        "Com. ", "PARCIAL ANUAL EMISION", "CARGO COBERTURA DE SEGURO"),
                DeductionType.TAXES, Set.of("Reten.ley"),
                DeductionType.INTEREST, Set.of("Interes"),
                DeductionType.NON_PAYMENT_FEE, Set.of("MORA"));
    }

}
