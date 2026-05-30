package tn.entreprise.escproject.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.entreprise.escproject.exception.BadRequestException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadService {

    @Value("${file.upload.dir:uploads}")
    private String uploadDirName;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif", "image/webp");
    private static final List<String> ALLOWED_VIDEO_TYPES = Arrays.asList("video/mp4", "video/mpeg", "video/quicktime", "video/x-msvideo", "video/webm");
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

    public String uploadFile(MultipartFile file, String type) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Le fichier ne peut pas etre vide");
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("Le fichier est trop volumineux. Taille maximale: 50MB");
        }

        // Validate content type
        if ("image".equals(type)) {
            if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
                throw new BadRequestException("Type d'image non supporte. Utilisez JPEG, PNG, GIF ou WebP");
            }
        } else if ("video".equals(type)) {
            if (!ALLOWED_VIDEO_TYPES.contains(file.getContentType())) {
                throw new BadRequestException("Type de video non supporte. Utilisez MP4, MPEG, MOV ou AVI");
            }
        }

        try {
            // Create upload directory with absolute path
            Path uploadPath = Paths.get(uploadDirName, type).toAbsolutePath();
            Files.createDirectories(uploadPath);

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ?
                    originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            // Save file
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath);

            // Return the file URL path (relative to the app, will be accessed via controller)
            return "/escproject/api/files/" + type + "/" + uniqueFilename;
        } catch (IOException e) {
            throw new BadRequestException("Erreur lors du telechargement du fichier: " + e.getMessage());
        }
    }

    public void deleteFile(String filePath) {
        try {
            if (filePath != null && !filePath.isEmpty()) {
                // Extract the filename from the path
                String filename = filePath.substring(filePath.lastIndexOf("/") + 1);
                String type = filePath.contains("/image/") ? "image" : "video";

                Path path = Paths.get(uploadDirName, type, filename).toAbsolutePath();
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            // Log error but don't throw - file deletion shouldn't break the main operation
            System.err.println("Erreur lors de la suppression du fichier: " + e.getMessage());
        }
    }
}
