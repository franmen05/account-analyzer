package com.guille.web;

import com.guille.domain.DeductionType;
import com.guille.domain.Summary;
import com.guille.service.AccountService;
import com.guille.service.FileUploadService;
import io.quarkus.logging.Log;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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
        var accountService = getAccountService(bank);
        var filePath = fileUploadService.uploadFile(file);

        Log.infof("Analyzing %s statement: %s", bank, filePath);
        try {

            var transactions = accountService.readFile(filePath, pass);

            var interest = accountService.getTransactionSummary(transactions, DeductionType.INTEREST);
            var taxes = accountService.getTransactionSummary(transactions, DeductionType.TAXES);
            var nonPaymentFee = accountService.getTransactionSummary(transactions, DeductionType.NON_PAYMENT_FEE);
            var commissions = accountService.getTransactionSummary(transactions, DeductionType.COMMISSIONS);
            var userInterests = accountService.getTransactionSummary(transactions, DeductionType.USER_INTEREST);
            var userInterestsOutTotal = accountService.getTransactionSummary(transactions, DeductionType.USER_INTEREST_OUT_TOTAL);

            Log.debugf("interest=%s taxes=%s nonPaymentFee=%s commissions=%s userInterest=%s userInterestOutTotal=%s",
                    interest, taxes, nonPaymentFee, commissions, userInterests, userInterestsOutTotal);

            return Summary.build(interest, taxes, nonPaymentFee, commissions, userInterests, userInterestsOutTotal);

        } catch (Exception e) {
            Log.error("Failed to analyze statement", e);
            throw new WebApplicationException("Could not analyze the statement: " + e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR);
        } finally {
            deleteQuietly(filePath);
        }

    }

    private void deleteQuietly(java.nio.file.Path filePath) {
        try {
            fileUploadService.delete(filePath);
        } catch (IOException e) {
            Log.warnf("Could not delete temporary file %s: %s", filePath, e.getMessage());
        }
    }

    private static String getBodyAsString(MultipartFormDataInput file, String field) throws IOException {
        return file.getFormDataMap().get(field).get(0).getBodyAsString();
    }

    private AccountService getAccountService(String name) {
        return handlers.stream()
                .filter(service -> service.getClass().getSimpleName().equals(name + SUFFIX))
                .findFirst()
                .orElseThrow(() -> new WebApplicationException(
                        "Unknown bank/format: " + name, Response.Status.BAD_REQUEST));
    }

    @GET
    @Path("/close")
    public String close(){
        System.exit(0);
        return "Closed";
    }
}