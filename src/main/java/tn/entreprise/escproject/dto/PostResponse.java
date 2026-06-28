package tn.entreprise.escproject.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties({"share"})
public class PostResponse {
    private Long id;
    private Long authorId;
    private String authorName;
    private String authorRole;
    private String authorProfilePictureUrl;
    private String content;
    private List<String> mediaUrls;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    private int likeCount;
    private int commentCount;
    private long shareCount;
    private boolean likedByCurrentUser;
    private boolean sharedByCurrentUser;
    @JsonProperty("isShare")
    private boolean isShare;
    private String shareText;
    private PostResponse originalPost;
}
