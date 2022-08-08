package com.guille.web;

import com.guille.domain.Account;
import com.guille.service.FileUploadService;
import com.opencsv.exceptions.CsvValidationException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("/hello")
public class GreetingResource {

    @Inject
    FileUploadService fileUploadService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() throws CsvValidationException, IOException {
        var transactions=fileUploadService.readPopularCSV();

        var InterestFinancing=transactions.stream()
                .filter(account -> account.Desc().equalsIgnoreCase("Interes Financiamiento"))
                .map(Account::amount)
                .reduce(0.0f, Float::sum);
        var InterestFinancing2=transactions.stream()
                .filter(account -> account.referenceNumber()==0)
                .map(Account::amount)
                .reduce(0.0f, Float::sum);

        System.out.println(InterestFinancing);
        System.out.println(InterestFinancing2);
        return "Hello RESTEasy";
    }
}
