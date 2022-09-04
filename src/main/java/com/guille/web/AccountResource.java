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
    @Path("/analyze")
    @Produces(MediaType.APPLICATION_JSON)
    public Summary analyze(@MultipartForm MultipartFormDataInput file) throws CsvValidationException, IOException {

        var  fileName=fileUploadService.uploadFile(file);

        System.out.println(fileName);

//        var transactions= accountService.readPopularCSV("pdcsvexport.csv");
//        var transactions= accountService.readPopularCSV("pdcsvexport(1).csv");
//        var transactions= accountService.readPopularCSV("pdcsvexport(2).csv");
        var transactions= accountService.readPopularCSV(fileName);

        var interest =  accountService.getTransactionSummary(transactions,TransactionType.INTEREST);
        var taxes = accountService.getTransactionSummary(transactions,TransactionType.TAXES);
        var nonPaymentFee = accountService.getTransactionSummary(transactions,TransactionType.NON_PAYMENT_FEE);
        var commissions = accountService.getTransactionSummary(transactions,TransactionType.COMMISSIONS);
        System.out.println("Intereses por financiamiento : " + interest.total());
        System.out.println("MORA : " + nonPaymentFee);
        System.out.println("Impuestos : " + taxes);
        System.out.println("Comisiones : " + commissions);

//        System.out.println(taxes);

        return  Summary.build(interest,taxes,nonPaymentFee,commissions);
    }



}
