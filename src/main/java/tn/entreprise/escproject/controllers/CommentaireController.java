package tn.entreprise.escproject.controllers;

import java.util.List;
import java.util.Map;

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
import tn.entreprise.escproject.dto.CommentaireDTO;
import tn.entreprise.escproject.dto.CommentaireRequest;
import tn.entreprise.escproject.services.Interfaces.ICommentaireService;

@RestController
@RequestMapping("/commentaires")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CommentaireController {

    private final ICommentaireService commentaireService;

    @PostMapping
    public ResponseEntity<ApiResponse> ajouterCommentaire(@Valid @RequestBody CommentaireRequest request) {
        CommentaireDTO commentaire = commentaireService.ajouterCommentaire(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Commentaire ajoute avec succes", commentaire));
    }

    @GetMapping("/publication/{publicationId}")
    public ResponseEntity<ApiResponse> getCommentairesByPublication(@PathVariable Long publicationId) {
        List<CommentaireDTO> commentaires = commentaireService.getCommentairesByPublication(publicationId);
        return ResponseEntity.ok(new ApiResponse(true, "Commentaires recuperes", commentaires));
    }

    @GetMapping("/publication/{publicationId}/paginated")
    public ResponseEntity<ApiResponse> getCommentairesPaginated(
            @PathVariable Long publicationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<CommentaireDTO> commentaires = commentaireService.getCommentairesPaginated(publicationId, page, size);
        return ResponseEntity.ok(new ApiResponse(true, "Commentaires pagines recuperes", commentaires));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateCommentaire(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        String contenu = (String) request.get("contenu");
        Long userId = Long.valueOf(request.get("userId").toString());

        CommentaireDTO commentaire = commentaireService.updateCommentaire(id, contenu, userId);
        return ResponseEntity.ok(new ApiResponse(true, "Commentaire modifie", commentaire));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteCommentaire(
            @PathVariable Long id,
            @RequestParam Long userId) {
        commentaireService.deleteCommentaire(id, userId);
        return ResponseEntity.ok(new ApiResponse(true, "Commentaire supprime avec succes", null));
    }
}