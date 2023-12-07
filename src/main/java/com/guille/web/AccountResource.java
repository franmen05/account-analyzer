package com.guille.web;

import com.guille.domain.DeductionType;
import com.guille.domain.Summary;
import com.guille.service.AccountService;
import com.guille.service.FileUploadService;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import java.io.IOException;

@Path("/account")
public class AccountResource {

    public static final String SUFFIX = "_ClientProxy";
    @Inject
    FileUploadService fileUploadService;

    @Inject
    Instance<AccountService> handlers;

    @POST
    @Path("/analyze")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Summary analyze(@MultipartForm MultipartFormDataInput file) throws IOException {

        var bank = getBodyAsString(file, "bank") + "AccountService";
        var pass = getBodyAsString(file, "pass");
//        System.out.println(bank);
        var accountService = getAccountService(bank);
//        System.out.println( accountService );
        var filePath = fileUploadService.uploadFile(file);

        System.out.println(filePath);
        try {

            var transactions = accountService.readFile(filePath, pass);

            var interest = accountService.getTransactionSummary(transactions, DeductionType.INTEREST);
            var taxes = accountService.getTransactionSummary(transactions, DeductionType.TAXES);
            var nonPaymentFee = accountService.getTransactionSummary(transactions, DeductionType.NON_PAYMENT_FEE);
            var commissions = accountService.getTransactionSummary(transactions, DeductionType.COMMISSIONS);
            var userInterests = accountService.getTransactionSummary(transactions, DeductionType.USER_INTEREST);
            var userInterestsOutTotal = accountService.getTransactionSummary(transactions, DeductionType.USER_INTEREST_OUT_TOTAL);

            System.out.println("Intereses por financiamiento : " + interest.total());
            System.out.println("MORA : " + nonPaymentFee);
            System.out.println("Impuestos : " + taxes);
            System.out.println("Comisiones : " + commissions);
            System.out.println("userInterest : " + userInterests);
            System.out.println("userInterestsOutTotal : " + userInterestsOutTotal);

            fileUploadService.delete(filePath);
            return Summary.build(interest, taxes, nonPaymentFee, commissions,userInterests,userInterestsOutTotal);

        } catch (Exception e) {
            System.out.println("Exception : ");
            e.printStackTrace();
            fileUploadService.delete(filePath);
//            Files.delete(fileName);

//            throw e;
            return null;

        }

    }

    private static String getBodyAsString(MultipartFormDataInput file, String field) throws IOException {
        return file.getFormDataMap().get(field).get(0).getBodyAsString();
    }

    private AccountService getAccountService(String NAME) {
        return handlers.stream()
                .filter(service -> service.getClass().getSimpleName().equals(NAME + SUFFIX))
                .findFirst().orElseThrow();
    }

    @GET
    @Path("/close")
    public String close(){
        System.exit(0);
        return "Closed";
    }
}