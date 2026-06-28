package tn.entreprise.escproject.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventRegistrationResponse {
    private Long id;
    private Long eventId;
    private String eventTitle;
    private Long userId;
    private String userName;
    private String userProfilePictureUrl;
    private LocalDateTime registeredAt;
}
