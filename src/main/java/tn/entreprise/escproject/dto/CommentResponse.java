package tn.entreprise.escproject.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentResponse {
    private Long id;
    private String content;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    private Long userId;
    private String userName;
    private String userRole;
    private String userProfilePictureUrl;
    private int likeCount;
    private boolean likedByCurrentUser;
}
