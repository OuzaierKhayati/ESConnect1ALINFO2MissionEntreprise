package tn.entreprise.escproject.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tn.entreprise.escproject.dto.ApiResponse;
import tn.entreprise.escproject.dto.PublicationDTO;
import tn.entreprise.escproject.dto.PublicationRequest;
import tn.entreprise.escproject.services.Interfaces.IPublicationService;

@RestController
@RequestMapping("/publications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PublicationController {

    private final IPublicationService publicationService;

    @PostMapping
    public ResponseEntity<ApiResponse> creerPublication(@Valid @RequestBody PublicationRequest request) {
        PublicationDTO publication = publicationService.creerPublication(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Publication creee avec succes", publication));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId) {
        Page<PublicationDTO> publications = publicationService.getFeed(page, size, userId);
        return ResponseEntity.ok(new ApiResponse(true, "Feed recupere avec succes", publications));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getPublicationById(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {
        PublicationDTO publication = publicationService.getPublicationById(id, userId);
        return ResponseEntity.ok(new ApiResponse(true, "Publication recuperee", publication));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> getPublicationsByUser(
            @PathVariable Long userId,
            @RequestParam(required = false) Long currentUserId) {
        List<PublicationDTO> publications = publicationService.getPublicationsByUser(userId, currentUserId);
        return ResponseEntity.ok(new ApiResponse(true, "Publications de l'utilisateur recuperees", publications));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updatePublication(
            @PathVariable Long id,
            @Valid @RequestBody PublicationRequest request) {
        PublicationDTO publication = publicationService.updatePublication(id, request, request.getAuteurId());
        return ResponseEntity.ok(new ApiResponse(true, "Publication mise a jour", publication));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deletePublication(
            @PathVariable Long id,
            @RequestParam Long userId) {
        publicationService.deletePublication(id, userId);
        return ResponseEntity.ok(new ApiResponse(true, "Publication supprimee avec succes", null));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchPublications(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId) {
        Page<PublicationDTO> publications = publicationService.searchPublications(keyword, page, size, userId);
        return ResponseEntity.ok(new ApiResponse(true, "Recherche effectuee", publications));
    }
}