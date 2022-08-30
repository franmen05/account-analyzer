package com.guille.web;

import com.guille.domain.Transaction;
import com.guille.domain.TransactionSummary;
import com.guille.service.AccountService;
import com.opencsv.exceptions.CsvValidationException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;

@Path("/account")
public class AccountResource {

    @Inject
    AccountService accountService;

    @GET
    @Path("/analyzer")
    @Produces(MediaType.APPLICATION_JSON)
    public TransactionSummary hello() throws CsvValidationException, IOException {

//        var transactions= accountService.readPopularCSV("pdcsvexport.csv");
//        var transactions= accountService.readPopularCSV("pdcsvexport(1).csv");
        var transactions= accountService.readPopularCSV("pdcsvexport(2).csv");

        var interest = getInterest(transactions);
        var taxes = getTaxes(transactions);
        var nonPaymentFee = getNonPaymentFee(transactions);
        System.out.println("Intereses por financiamiento : " + interest);
        System.out.println("MORA : " + nonPaymentFee);
        System.out.println("Impuestos : " + taxes);

//        System.out.println(taxes);

        return new TransactionSummary(interest,taxes,nonPaymentFee);
    }

    private static Float getInterest(List<Transaction> transactions) {
//        var interest= transactions.stream()
//                .filter(account -> account.referenceNumber() == 0) // no se puede usar porque los pagos  tambien se  registran con 0
//                .map(Transaction::amount)
//                .reduce(0.0f, Float::sum);;
//
//        if(interest==0.0f)


        return transactions.stream()
                .filter(account -> account.descContains("Interes Financiamiento"))
                .map(Transaction::amount)
                .reduce(0.0f, Float::sum);
    }

    private static Float getNonPaymentFee(List<Transaction> transactions) {

        return transactions.stream()
                .filter(account -> account.descContains("MORA"))
                .map(Transaction::amount)
                .reduce(0.0f, Float::sum);
    }
    private static Float getTaxes(List<Transaction> transactions) {

        return transactions.stream()
                    .filter(account -> account.descContains("IMPUESTO")
                        || account.descContains("SOBREGIRO")
                        || account.descContains("CARGO POR SERVICIO")
                    )
                    .map(Transaction::amount)
                    .reduce(0.0f, Float::sum);
    }
}
