package tn.entreprise.escproject.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tn.entreprise.escproject.dto.*;
import tn.entreprise.escproject.entite.*;
import tn.entreprise.escproject.exception.BadRequestException;
import tn.entreprise.escproject.exception.ResourceNotFoundException;
import tn.entreprise.escproject.exception.UnauthorizedException;
import tn.entreprise.escproject.repositories.PostCommentRepository;
import tn.entreprise.escproject.repositories.PostRepository;
import tn.entreprise.escproject.repositories.UserRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostServiceImp {

    private static final Logger log = LoggerFactory.getLogger(PostServiceImp.class);

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostCommentRepository postCommentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UploadService uploadService;

    // ======== Post CRUD ========

    @Transactional
    public PostResponse createPost(PostRequest request, Long authorId) {
        if (request.getContent() == null && (request.getMediaUrls() == null || request.getMediaUrls().isEmpty())
                && request.getOriginalPostId() == null) {
            throw new BadRequestException("Post must have content or media");
        }

        User author = findUserOrThrow(authorId);

        Post post = new Post();
        post.setContent(request.getContent());
        post.setMediaUrls(request.getMediaUrls() != null ? request.getMediaUrls() : new ArrayList<>());
        post.setCreatedAt(LocalDateTime.now());
        post.setAuthor(author);

        if (request.getOriginalPostId() != null) {
            Post original = findPostOrThrow(request.getOriginalPostId());
            post.setOriginalPost(original);
            post.setShareText(request.getShareText());
        }

        postRepository.save(post);
        log.info("Post created by user {}", authorId);
        return toResponse(post, authorId);
    }

    @Transactional
    public PostResponse updatePost(Long postId, PostRequest request, Long authorId) {
        Post post = findPostOrThrow(postId);
        if (!post.getAuthor().getId().equals(authorId)) {
            throw new UnauthorizedException("You can only edit your own posts");
        }
        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }
        if (request.getMediaUrls() != null) {
            post.setMediaUrls(request.getMediaUrls());
        }
        postRepository.save(post);
        return toResponse(post, authorId);
    }

    @Transactional
    public void deletePost(Long postId, Long authorId) {
        Post post = findPostOrThrow(postId);
        if (!post.getAuthor().getId().equals(authorId)) {
            throw new UnauthorizedException("You can only delete your own posts");
        }
        postRepository.delete(post);
        log.info("Post {} deleted by user {}", postId, authorId);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts(Long currentUserId) {
        return postRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(p -> toResponse(p, currentUserId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getMyPosts(Long userId) {
        User user = findUserOrThrow(userId);
        return postRepository.findByAuthorOrderByCreatedAtDesc(user).stream()
                .map(p -> toResponse(p, userId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PostResponse getPostById(Long postId, Long currentUserId) {
        return toResponse(findPostOrThrow(postId), currentUserId);
    }

    // ======== Media Upload ========

    public List<String> uploadMedia(MultipartFile[] files) throws IOException {
        if (files == null || files.length == 0) {
            throw new BadRequestException("No files provided");
        }
        if (files.length > 4) {
            throw new BadRequestException("Maximum 4 files per post");
        }

        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            String contentType = file.getContentType();
            if (contentType == null) {
                throw new BadRequestException("Invalid file type");
            }
            boolean isImage = contentType.startsWith("image/");
            boolean isVideo = contentType.startsWith("video/");
            if (!isImage && !isVideo) {
                throw new BadRequestException("Only image and video files are allowed");
            }
            if (isVideo && files.length > 1) {
                throw new BadRequestException("Only one video per post is allowed");
            }
            urls.add(uploadService.uploadFile(file));
        }
        return urls;
    }

    // ======== Likes ========

    @Transactional
    public boolean toggleLike(Long postId, Long userId) {
        Post post = findPostOrThrow(postId);
        User user = findUserOrThrow(userId);

        if (post.getLikedBy() == null) {
            post.setLikedBy(new ArrayList<>());
        }

        boolean alreadyLiked = post.getLikedBy().stream().anyMatch(u -> u.getId().equals(userId));
        if (alreadyLiked) {
            post.getLikedBy().removeIf(u -> u.getId().equals(userId));
            postRepository.save(post);
            return false;
        } else {
            post.getLikedBy().add(user);
            postRepository.save(post);
            return true;
        }
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getPostLikers(Long postId) {
        Post post = findPostOrThrow(postId);
        return post.getLikedBy().stream()
                .map(u -> {
                    UserResponse r = new UserResponse();
                    r.setId(u.getId());
                    r.setFirstName(u.getFirstName());
                    r.setLastName(u.getLastName());
                    r.setEmail(u.getEmail());
                    r.setRoleUser(u.getRoleUser() != null ? u.getRoleUser().toString() : null);
                    return r;
                })
                .collect(Collectors.toList());
    }

    // ======== Comments ========

    @Transactional
    public CommentResponse addComment(Long postId, Long userId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new BadRequestException("Comment content cannot be empty");
        }
        Post post = findPostOrThrow(postId);
        User user = findUserOrThrow(userId);

        PostComment comment = new PostComment();
        comment.setContent(content.trim());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUser(user);
        comment.setPost(post);

        postCommentRepository.save(comment);

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .userId(user.getId())
                .userName(user.getFirstName() + " " + user.getLastName())
                .userRole(user.getRoleUser() != null ? user.getRoleUser().toString() : null)
                .userProfilePictureUrl(user.getProfile() != null ? user.getProfile().getProfilePictureUrl() : null)
                .likeCount(0)
                .likedByCurrentUser(false)
                .build();
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long postId, Long currentUserId) {
        Post post = findPostOrThrow(postId);
        return postCommentRepository.findByPostOrderByCreatedAtDesc(post).stream()
                .map(c -> CommentResponse.builder()
                        .id(c.getId())
                        .content(c.getContent())
                        .createdAt(c.getCreatedAt())
                        .userId(c.getUser().getId())
                        .userName(c.getUser().getFirstName() + " " + c.getUser().getLastName())
                        .userRole(c.getUser().getRoleUser() != null ? c.getUser().getRoleUser().toString() : null)
                        .userProfilePictureUrl(c.getUser().getProfile() != null ? c.getUser().getProfile().getProfilePictureUrl() : null)
                        .likeCount(c.getLikedBy() != null ? c.getLikedBy().size() : 0)
                        .likedByCurrentUser(currentUserId != null && c.getLikedBy() != null
                                && c.getLikedBy().stream().anyMatch(u -> u.getId().equals(currentUserId)))
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponse toggleCommentLike(Long commentId, Long userId) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));
        User user = findUserOrThrow(userId);

        if (comment.getLikedBy() == null) {
            comment.setLikedBy(new ArrayList<>());
        }

        boolean alreadyLiked = comment.getLikedBy().stream().anyMatch(u -> u.getId().equals(userId));
        if (alreadyLiked) {
            comment.getLikedBy().removeIf(u -> u.getId().equals(userId));
        } else {
            comment.getLikedBy().add(user);
        }
        postCommentRepository.save(comment);

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .userId(comment.getUser().getId())
                .userName(comment.getUser().getFirstName() + " " + comment.getUser().getLastName())
                .likeCount(comment.getLikedBy().size())
                .likedByCurrentUser(!alreadyLiked)
                .build();
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));
        if (!comment.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own comments");
        }
        postCommentRepository.delete(comment);
    }

    // ======== Helpers ========

    private Post findPostOrThrow(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + id));
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    @Transactional(readOnly = true)
    public PostResponse toResponse(Post post, Long currentUserId) {
        List<User> likedBy = post.getLikedBy() != null ? new ArrayList<>(post.getLikedBy()) : List.of();
        List<PostComment> comments = post.getComments() != null ? new ArrayList<>(post.getComments()) : List.of();
        long shareCount = postRepository.countByOriginalPost(post);

        boolean liked = currentUserId != null && likedBy.stream().anyMatch(u -> u.getId().equals(currentUserId));
        boolean shared = currentUserId != null
                && postRepository.existsByOriginalPostIdAndAuthorId(post.getId(), currentUserId);

        PostResponse.PostResponseBuilder builder = PostResponse.builder()
                .id(post.getId())
                .authorId(post.getAuthor() != null ? post.getAuthor().getId() : null)
                .authorName(post.getAuthor() != null ? post.getAuthor().getFirstName() + " " + post.getAuthor().getLastName() : null)
                .authorRole(post.getAuthor() != null && post.getAuthor().getRoleUser() != null ? post.getAuthor().getRoleUser().toString() : null)
                .authorProfilePictureUrl(post.getAuthor() != null && post.getAuthor().getProfile() != null ? post.getAuthor().getProfile().getProfilePictureUrl() : null)
                .content(post.getContent())
                .mediaUrls(new ArrayList<>(post.getMediaUrls()))
                .createdAt(post.getCreatedAt())
                .likeCount(likedBy.size())
                .commentCount(comments.size())
                .shareCount(shareCount)
                .likedByCurrentUser(liked)
                .sharedByCurrentUser(shared)
                .isShare(post.getOriginalPost() != null)
                .shareText(post.getShareText());

        if (post.getOriginalPost() != null) {
            builder.originalPost(toOriginalPostResponse(post.getOriginalPost()));
        }

        return builder.build();
    }

    private PostResponse toOriginalPostResponse(Post post) {
        List<User> likedBy = post.getLikedBy() != null ? new ArrayList<>(post.getLikedBy()) : List.of();
        List<PostComment> comments = post.getComments() != null ? new ArrayList<>(post.getComments()) : List.of();
        return PostResponse.builder()
                .id(post.getId())
                .authorId(post.getAuthor() != null ? post.getAuthor().getId() : null)
                .authorName(post.getAuthor() != null ? post.getAuthor().getFirstName() + " " + post.getAuthor().getLastName() : null)
                .authorRole(post.getAuthor() != null && post.getAuthor().getRoleUser() != null ? post.getAuthor().getRoleUser().toString() : null)
                .authorProfilePictureUrl(post.getAuthor() != null && post.getAuthor().getProfile() != null ? post.getAuthor().getProfile().getProfilePictureUrl() : null)
                .content(post.getContent())
                .mediaUrls(new ArrayList<>(post.getMediaUrls()))
                .createdAt(post.getCreatedAt())
                .likeCount(likedBy.size())
                .commentCount(comments.size())
                .shareCount(0)
                .build();
    }
}
