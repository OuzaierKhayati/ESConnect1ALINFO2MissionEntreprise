package tn.entreprise.escproject.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class UploadService {

    private final String UPLOAD_DIR =
            "uploads/files/";

    private final String PUBLIC_UPLOAD_PATH =
            "/uploads/files/";

    public String uploadFile(
            MultipartFile file) throws IOException {

        String fileName =
                System.currentTimeMillis()
                        + "_"
                        + file.getOriginalFilename();

        Path path = Paths.get(
                UPLOAD_DIR,
                fileName
        );

        Files.createDirectories(
                path.getParent()
        );

        Files.write(
                path,
                file.getBytes()
        );

        return PUBLIC_UPLOAD_PATH + fileName;
    }
}
