package tn.entreprise.escproject.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tn.entreprise.escproject.dto.*;
import tn.entreprise.escproject.entite.User;
import tn.entreprise.escproject.exception.ResourceNotFoundException;
import tn.entreprise.escproject.repositories.UserRepository;
import tn.entreprise.escproject.services.JobOfferServiceImp;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/jobs")
public class JobOfferController {

    @Autowired
    private JobOfferServiceImp jobOfferService;

    @Autowired
    private UserRepository userRepository;

    // ===== Job CRUD =====

    @PostMapping
    public ResponseEntity<ApiResponse<JobOfferResponse>> createJob(
            @Valid @RequestBody JobOfferRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        JobOfferResponse response = jobOfferService.createJob(request, user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Job offer created successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JobOfferResponse>> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody JobOfferRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        JobOfferResponse response = jobOfferService.updateJob(id, request, user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Job offer updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteJob(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        jobOfferService.deleteJob(id, user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Job offer deleted successfully", null));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<JobOfferResponse>> toggleJobStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        JobOfferResponse response = jobOfferService.toggleJobStatus(id, user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Job status updated", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<JobOfferResponse>>> getAllJobs(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        List<JobOfferResponse> jobs = jobOfferService.getAllJobs(user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Jobs retrieved successfully", jobs));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<JobOfferResponse>>> getMyJobs(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        List<JobOfferResponse> jobs = jobOfferService.getMyJobs(user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Your job posts retrieved successfully", jobs));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobOfferResponse>> getJobById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        JobOfferResponse job = jobOfferService.getJobById(id, user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Job retrieved successfully", job));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<JobOfferResponse>>> searchJobs(
            @RequestParam String query,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        List<JobOfferResponse> jobs = jobOfferService.searchJobs(query, user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Search results", jobs));
    }

    // ===== Social Features =====

    @PostMapping("/{id}/like")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> toggleLike(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        boolean liked = jobOfferService.toggleLike(id, user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true,
                liked ? "Job liked" : "Job unliked",
                Map.of("liked", liked)));
    }

    @GetMapping("/{id}/likers")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getJobLikers(@PathVariable Long id) {
        List<UserResponse> likers = jobOfferService.getJobLikers(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Likers retrieved", likers));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        String content = body.get("content");
        CommentResponse comment = jobOfferService.addComment(id, user.getId(), content);
        return ResponseEntity.ok(new ApiResponse<>(true, "Comment added", comment));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        List<CommentResponse> comments = jobOfferService.getComments(id, user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Comments retrieved", comments));
    }

    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<ApiResponse<CommentResponse>> toggleCommentLike(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        CommentResponse comment = jobOfferService.toggleCommentLike(commentId, user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true,
                comment.isLikedByCurrentUser() ? "Comment liked" : "Comment unliked", comment));
    }

    // ===== Helper =====

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
