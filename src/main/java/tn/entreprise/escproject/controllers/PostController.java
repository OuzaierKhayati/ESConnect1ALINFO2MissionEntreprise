package tn.entreprise.escproject.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.entreprise.escproject.dto.*;
import tn.entreprise.escproject.entite.User;
import tn.entreprise.escproject.exception.ResourceNotFoundException;
import tn.entreprise.escproject.repositories.UserRepository;
import tn.entreprise.escproject.services.PostServiceImp;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostServiceImp postService;

    @Autowired
    private UserRepository userRepository;

    // ===== CRUD =====

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @RequestBody PostRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        PostResponse response = postService.createPost(request, user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Post created successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable Long id,
            @RequestBody PostRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        PostResponse response = postService.updatePost(id, request, user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Post updated", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        postService.deletePost(id, user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Post deleted", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PostResponse>>> getAllPosts(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        List<PostResponse> posts = postService.getAllPosts(user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Posts retrieved", posts));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getMyPosts(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        List<PostResponse> posts = postService.getMyPosts(user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Your posts retrieved", posts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> getPostById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        PostResponse post = postService.getPostById(id, user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Post retrieved", post));
    }

    // ===== Media Upload =====

    @PostMapping("/upload-media")
    public ResponseEntity<ApiResponse<List<String>>> uploadMedia(
            @RequestParam("files") MultipartFile[] files) throws IOException {
        List<String> urls = postService.uploadMedia(files);
        return ResponseEntity.ok(new ApiResponse<>(true, "Files uploaded", urls));
    }

    // ===== Likes =====

    @PostMapping("/{id}/like")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> toggleLike(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        boolean liked = postService.toggleLike(id, user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true,
                liked ? "Post liked" : "Post unliked",
                Map.of("liked", liked)));
    }

    @GetMapping("/{id}/likers")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getLikers(@PathVariable Long id) {
        List<UserResponse> likers = postService.getPostLikers(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Likers retrieved", likers));
    }

    // ===== Comments =====

    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        CommentResponse comment = postService.addComment(id, user.getId(), body.get("content"));
        return ResponseEntity.ok(new ApiResponse<>(true, "Comment added", comment));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        List<CommentResponse> comments = postService.getComments(id, user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Comments retrieved", comments));
    }

    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<ApiResponse<CommentResponse>> toggleCommentLike(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        CommentResponse comment = postService.toggleCommentLike(commentId, user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true,
                comment.isLikedByCurrentUser() ? "Comment liked" : "Comment unliked", comment));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        postService.deleteComment(commentId, user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Comment deleted", null));
    }

    // ===== Share =====

    @PostMapping("/{id}/share")
    public ResponseEntity<ApiResponse<PostResponse>> sharePost(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        PostRequest request = new PostRequest();
        request.setOriginalPostId(id);
        request.setShareText(body != null ? body.get("shareText") : null);
        PostResponse response = postService.createPost(request, user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Post shared", response));
    }

    // ===== Helper =====

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
