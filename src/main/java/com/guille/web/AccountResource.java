package com.guille.web;

import com.guille.domain.Summary;
import com.guille.domain.Transaction;
import com.guille.domain.TransactionSummary;
import com.guille.service.AccountService;
import com.guille.service.FileUploadService;
import com.opencsv.exceptions.CsvValidationException;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

@Path("/account")
public class AccountResource {

    @Inject
    AccountService accountService;

    @Inject
    FileUploadService fileUploadService;

    @POST
    @Path("/analyzer")
    @Produces(MediaType.APPLICATION_JSON)
    public Summary analyzer(@MultipartForm MultipartFormDataInput file) throws CsvValidationException, IOException {

        var  fileName=fileUploadService.uploadFile(file);

        System.out.println(fileName);

//        var transactions= accountService.readPopularCSV("pdcsvexport.csv");
//        var transactions= accountService.readPopularCSV("pdcsvexport(1).csv");
//        var transactions= accountService.readPopularCSV("pdcsvexport(2).csv");
        var transactions= accountService.readPopularCSV(fileName);

        var interest = getInterest(transactions);
        var taxes = getTaxes(transactions);
        var nonPaymentFee = getNonPaymentFee(transactions);
        var commissions = getCommissions(transactions);
        System.out.println("Intereses por financiamiento : " + interest.total());
        System.out.println("MORA : " + nonPaymentFee);
        System.out.println("Impuestos : " + taxes);
        System.out.println("Comisiones : " + commissions);

//        System.out.println(taxes);

        return  Summary.build(interest,taxes,nonPaymentFee,commissions);
    }

    private static TransactionSummary getInterest(List<Transaction> transactions) {

        var  transactionDesList= new HashSet<String>();

        var  total= transactions.stream()
                .filter(account -> account.descContains("Interes"))
                .map(t ->{
                    transactionDesList.add(t.desc());
                    return t.amount();
                } )
                .reduce(0.0f, Float::sum);

        return new TransactionSummary(transactionDesList,total);
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
                    )
                    .map(Transaction::amount)
                    .reduce(0.0f, Float::sum);
    }

    private static Float getCommissions(List<Transaction> transactions) {

        return transactions.stream()
                    .filter(account -> account.descContains("SOBREGIRO")
                        || account.descContains("CARGO POR SERVICIO")
                        || account.descContains("CARGO POR SERV")
                        || account.descContains("COMISIONES")
                    )
                    .map(Transaction::amount)
                    .reduce(0.0f, Float::sum);
    }
}
