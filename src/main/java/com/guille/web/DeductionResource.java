package com.guille.web;

import com.guille.domain.Deduction;
import com.guille.domain.DeductionType;
import com.guille.service.DeductionService;
import com.guille.web.dto.DeductionDTO;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;


@Path("/deduction")
public class DeductionResource {

    @Inject
    DeductionService deductionService;

    @POST
    @Path("/")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create( @MultipartForm MultipartFormDataInput file) throws  IOException {

        var d=  Deduction.build(DeductionType.valueOf(getBodyAsString(file,"type")),getBodyAsString(file,"description"));
        deductionService.create(d);
        return Response.ok(d).build();

    }

    private static String getBodyAsString(MultipartFormDataInput file, String field) throws IOException {
        return file.getFormDataMap().get(field).get(0).getBodyAsString();
    }

    @GET
    @Path("/list")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response list( ) throws  IOException {

        var deductions = deductionService.listAll();
        return Response.ok(new DeductionDTO(deductions)).build();
    }

}
