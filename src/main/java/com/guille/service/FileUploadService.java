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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

@ApplicationScoped
public class FileUploadService {

    @Inject
    Constants constants;

    public String uploadFile(MultipartFormDataInput input) {
        var uploadForm = input.getFormDataMap();
        var fileNames = new ArrayList<>();
        var inputParts = uploadForm.get("file");
        String fileName = null;
        for (InputPart inputPart : inputParts) {
            try {
                var header = inputPart.getHeaders();
                fileName = getFileName(header);
                fileNames.add(fileName);
                var inputStream = inputPart.getBody(InputStream.class, null);
                return writeFile(inputStream,fileName).toString();
            } catch (Exception e) {
                e.printStackTrace();
                return "Can't save file";
            }
        }
        return "Files Successfully Uploaded";
    }

    private Path writeFile(InputStream inputStream, String fileName) throws IOException {
        byte[] bytes = IOUtils.toByteArray(inputStream);
        File customDir = new File(constants.uploadDir());
        fileName = customDir.getAbsolutePath() + File.separator + fileName;
        Path path = Paths.get(fileName);
        Files.deleteIfExists(path);
        return Files.write(path, bytes, StandardOpenOption.CREATE_NEW);
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
