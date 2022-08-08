package com.guille.service;

import com.guille.domain.Account;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.MultivaluedMap;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class FileUploadService {

    @ConfigProperty(name = "upload.directory")
    String UPLOAD_DIR;

    public String uploadFile(MultipartFormDataInput input) {
        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        List<String> fileNames = new ArrayList<>();
        List<InputPart> inputParts = uploadForm.get("file");
        String fileName = null;
        for (InputPart inputPart : inputParts) {
            try {
                MultivaluedMap<String, String> header = inputPart.getHeaders();
                fileName = getFileName(header);
                fileNames.add(fileName);
                InputStream inputStream = inputPart.getBody(InputStream.class, null);
                writeFile(inputStream,fileName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "Files Successfully Uploaded";
    }

    private void writeFile(InputStream inputStream,String fileName) throws IOException {
        byte[] bytes = IOUtils.toByteArray(inputStream);
        File customDir = new File(UPLOAD_DIR);

        fileName = customDir.getAbsolutePath() + File.separator + fileName;
        Files.write(Paths.get(fileName), bytes,
                StandardOpenOption.CREATE_NEW);
    }

    private String getFileName(MultivaluedMap<String, String> header) {
        String[] contentDisposition = header.
                getFirst("Content-Disposition").split(";");
        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {
                var name = filename.split("=");
                var finalFileName = name[1].trim().replaceAll("\"", "");
                return finalFileName;
            }
        }
        return "";
    }

    public List<Account> readPopularCSV() throws IOException, CsvValidationException {

        var reader = new CSVReader(new FileReader(UPLOAD_DIR+"/pdcsvexport.csv"));

        var transactions = new ArrayList<Account>();


        for(int i=0;i<11;i++)
            reader.readNext();

        // read line by line
        String[] record = null;
        while ((record = reader.readNext()) != null) {
            if("".equals(record[1].trim()))
                break;
            var a = new Account(record[0],
                    record[1],
                    Float.parseFloat(record[2]),
                    Integer.parseInt(record[3]),
                    record[4],
                    record[5]);
            transactions.add(a);
//            System.out.println(Arrays.toString(record));
            System.out.println(a);
        }


        reader.close();
        return transactions;
    }

}
