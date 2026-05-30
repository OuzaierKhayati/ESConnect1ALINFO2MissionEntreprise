package tn.entreprise.escproject.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import tn.entreprise.escproject.dto.ApiResponse;
import tn.entreprise.escproject.services.FileUploadService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @Value("${file.upload.dir:uploads}")
    private String uploadDirName;

    @PostMapping("/image")
    public ResponseEntity<ApiResponse> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String filePath = fileUploadService.uploadFile(file, "image");
            Map<String, String> response = new HashMap<>();
            response.put("url", filePath);
            response.put("filename", file.getOriginalFilename());
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Image telechargee avec succes", response));
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @PostMapping("/video")
    public ResponseEntity<ApiResponse> uploadVideo(@RequestParam("file") MultipartFile file) {
        try {
            String filePath = fileUploadService.uploadFile(file, "video");
            Map<String, String> response = new HashMap<>();
            response.put("url", filePath);
            response.put("filename", file.getOriginalFilename());
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Video telechargee avec succes", response));
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse> deleteFile(@RequestParam String filePath) {
        try {
            fileUploadService.deleteFile(filePath);
            return ResponseEntity.ok(new ApiResponse(true, "Fichier supprime avec succes", null));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Erreur lors de la suppression: " + e.getMessage(), null));
        }
    }

    /**
     * Serve uploaded files - accessible at /escproject/api/files/{type}/{filename}
     */
    @GetMapping("/{type}/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String type, @PathVariable String filename) {
        try {
            // Validate type
            if (!("image".equals(type) || "video".equals(type))) {
                return ResponseEntity.notFound().build();
            }

            // Resolve file path
            Path filePath = Paths.get(uploadDirName, type, filename).toAbsolutePath();
            File file = filePath.toFile();

            // Check if file exists
            if (!file.exists() || !file.isFile()) {
                return ResponseEntity.notFound().build();
            }

            // Determine media type
            String contentType = "image".equals(type) ? "image/jpeg" : "video/mp4";
            if (filename.endsWith(".png")) contentType = "image/png";
            else if (filename.endsWith(".gif")) contentType = "image/gif";
            else if (filename.endsWith(".webp")) contentType = "image/webp";
            else if (filename.endsWith(".mp4")) contentType = "video/mp4";
            else if (filename.endsWith(".webm")) contentType = "video/webm";
            else if (filename.endsWith(".mpeg")) contentType = "video/mpeg";

            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
