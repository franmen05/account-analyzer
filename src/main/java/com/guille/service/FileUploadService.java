package com.guille.service;

import com.guille.config.Constants;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedMap;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;

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
                fileName = customDir.getAbsolutePath() + File.separator + fileName;
//                fileNames.add(fileName);
//                var path = Paths.get(fileName);
                System.out.println(fileName);
//                delete(fileName);
//                var file= new File(fileName);
//                file.deleteOnExit();
//                if (file.delete()) {
//                    System.out.println("Deleted the file: " + file.getName());
//                } else {
//                    System.out.println("Failed to delete the file.");
//                }

                var inputStream = inputPart.getBody(InputStream.class, null);
                var filePath= writeFile(inputStream,fileName);
//                filePath.toFile().deleteOnExit();
                return filePath;

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
        byte[] bytes = IOUtils.toByteArray(inputStream);

        return Files.write(Paths.get(fileName), bytes, StandardOpenOption.CREATE_NEW);
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
