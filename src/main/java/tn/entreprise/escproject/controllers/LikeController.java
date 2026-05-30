package tn.entreprise.escproject.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import tn.entreprise.escproject.dto.ApiResponse;
import tn.entreprise.escproject.dto.LikeResponse;
import tn.entreprise.escproject.services.Interfaces.ILikeService;

@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LikeController {

    private final ILikeService likeService;

    @PostMapping("/{publicationId}/toggle")
    public ResponseEntity<ApiResponse> toggleLike(
            @PathVariable Long publicationId,
            @RequestParam Long userId) {
        LikeResponse response = likeService.toggleLike(publicationId, userId);
        String message = response.isLiked() ? "Publication likee" : "Like retire";
        return ResponseEntity.ok(new ApiResponse(true, message, response));
    }

    @GetMapping("/{publicationId}/check")
    public ResponseEntity<ApiResponse> checkUserLike(
            @PathVariable Long publicationId,
            @RequestParam Long userId) {
        boolean hasLiked = likeService.hasUserLiked(publicationId, userId);
        long totalLikes = likeService.countLikes(publicationId);

        LikeResponse response = LikeResponse.builder()
                .liked(hasLiked)
                .totalLikes(totalLikes)
                .build();

        return ResponseEntity.ok(new ApiResponse(true, "Verification effectuee", response));
    }

    @GetMapping("/{publicationId}/count")
    public ResponseEntity<ApiResponse> countLikes(@PathVariable Long publicationId) {
        long count = likeService.countLikes(publicationId);

        LikeResponse response = LikeResponse.builder()
                .totalLikes(count)
                .build();

        return ResponseEntity.ok(new ApiResponse(true, "Nombre de likes recupere", response));
    }
}