package com.guille.service.bank;

import com.guille.domain.DeductionType;
import com.guille.domain.Transaction;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@ApplicationScoped
public class PopularAccountService extends BaseBankService {

    public List<Transaction> readFile(Path filePath,String... additionalParam) throws IOException {

        try (var reader = new CSVReader(new FileReader(filePath.toFile()))) {
            var transactions = new ArrayList<Transaction>();
            for (int i = 0; i < 11; i++)
                reader.readNext();

            // read line by line
            String[] record;
            while ((record = reader.readNext()) != null) {
                Log.debugf("%d :: %s", record.length, Arrays.toString(record));
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
            }

            return transactions;
        } catch (CsvValidationException e) {
            var ex = new IOException(e.getMessage());
            ex.setStackTrace(e.getStackTrace());
            throw ex;
        }
    }


    @Override
    protected Map<DeductionType, Set<String>> keywordsByType() {
        return Map.of(
                DeductionType.COMMISSIONS, Set.of(
                        "SOBREGIRO", "CARGO POR SERV", "CARGO EMISION", "PERDIDA",
                        "COMISIONES", "SEGURO DEUDOR", "COM. AVANCE"),
                DeductionType.TAXES, Set.of("IMPUESTO"),
                DeductionType.INTEREST, Set.of("Interes"),
                DeductionType.NON_PAYMENT_FEE, Set.of("MORA"));
    }
}
