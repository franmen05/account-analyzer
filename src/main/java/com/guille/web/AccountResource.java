package com.guille.web;

import com.guille.domain.Summary;
import com.guille.domain.TransactionType;
import com.guille.service.AccountService;
import com.guille.service.BHDAccountService;
import com.guille.service.PopularAccountService;
import com.guille.service.FileUploadService;
import com.opencsv.exceptions.CsvValidationException;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Handler;

@Path("/account")
public class AccountResource {


    public static final String SUFFIX = "_ClientProxy";
    @Inject
    FileUploadService fileUploadService;

    @Inject
    Instance<AccountService> handlers;

    private AccountService accountService;

    //    @GET
//    @Produces(MediaType.TEXT_PLAIN)
//    public List<String> handle() {
//        var   list = new ArrayList<String>();
//        handlers.forEach(handler -> {
//        System.out.println(PopularAccountService.class.getSimpleName()+"_ClientProxy");
//        System.out.println(handler.getClass().getSimpleName());
//
//            list.add(handler.getClass().getSimpleName());
//        });
//
//        return list;
//    }
    @POST
    @Path("/analyze")
    @Produces(MediaType.APPLICATION_JSON)
    public Summary analyze(@MultipartForm MultipartFormDataInput file) throws CsvValidationException, IOException {

        var bank = file.getFormDataMap().get("bank").get(0).getBodyAsString()+"AccountService";

//        System.out.println(bank);
        if(PopularAccountService.NAME.equals(bank))
            accountService = getAccountService(PopularAccountService.NAME);
        else if(BHDAccountService.NAME.equals(bank))
            accountService = getAccountService(BHDAccountService.NAME);
//        System.out.println(BHDAccountService.NAME);

        var  fileName=fileUploadService.uploadFile(file);

        System.out.println(fileName);

//        var transactions= accountService.readPopularCSV("pdcsvexport.csv");
//        var transactions= accountService.readPopularCSV("pdcsvexport(1).csv");
//        var transactions= accountService.readPopularCSV("pdcsvexport(2).csv");
        var transactions= accountService.readCSV(fileName);

        var interest =  accountService.getTransactionSummary(transactions, TransactionType.INTEREST);
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

    private AccountService getAccountService(String NAME) {
        return handlers.stream()
                .filter(service -> service.getClass().getSimpleName().equals(NAME + SUFFIX))
                .findFirst().orElseThrow();
    }


}
