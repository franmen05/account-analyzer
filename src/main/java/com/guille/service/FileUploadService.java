package com.guille.service;

import com.guille.config.Constants;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedMap;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@ApplicationScoped
public class FileUploadService {

    @Inject
    Constants constants;


    public Path uploadFile(MultipartFormDataInput input) {
        var uploadForm = input.getFormDataMap();
//        var fileNames = new ArrayList<>();
        var inputParts = uploadForm.get("file");
        String fileName = null;


        for (var inputPart : inputParts) {

            try {

                fileName = getFileName(inputPart.getHeaders());
                var customDir = new File(constants.uploadDir());
                if (customDir.mkdirs())
                    System.out.println("Directory "+customDir+" Create");

                fileName = customDir.getAbsolutePath() + File.separator + fileName;
                System.out.println(fileName);


                var inputStream = inputPart.getBody(InputStream.class, null);
                //                filePath.toFile().deleteOnExit();
                return writeFile(inputStream,fileName);

            } catch (Exception e) {
                e.printStackTrace();

                return null;
            }
        }
        return null;
    }
    public void delete(Path filePath) throws IOException {

        Files.deleteIfExists(filePath);
    }

    private Path writeFile(InputStream inputStream, String fileName) throws IOException {
        try {
            Path path = Paths.get(fileName);
            delete(path);

            byte[] bytes = IOUtils.toByteArray(inputStream);

            return Files.write(path, bytes, StandardOpenOption.CREATE_NEW);
        }catch (IOException e){
            inputStream.close();
            throw e;
        }
    }

    private String getFileName(MultivaluedMap<String, String> header) {
        String[] contentDisposition = header.getFirst("Content-Disposition").split(";");
        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {
                var name = filename.split("=");
                return name[1].trim().replaceAll("\"", "");
            }
        }
        return "";
    }


}
