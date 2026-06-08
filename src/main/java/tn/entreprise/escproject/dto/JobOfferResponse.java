package tn.entreprise.escproject.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobOfferResponse {
    private Long id;
    private String title;
    private String description;
    private String company;
    private String location;
    private LocalDateTime createdAt;
    private String jobType;
    private String status;
    private String requirements;
    private String salary;

    // Recruiter info
    private Long recruiterId;
    private String recruiterName;
    private String recruiterEmail;
    private String recruiterProfilePictureUrl;

    // Counts
    private int applicationCount;
    private int likeCount;
    private int commentCount;

    // Current user state
    private boolean likedByCurrentUser;
    private boolean appliedByCurrentUser;
}
