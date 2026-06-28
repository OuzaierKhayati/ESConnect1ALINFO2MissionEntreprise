package tn.entreprise.escproject.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private String eventType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String location;
    private boolean online;
    private String meetingLink;
    private int capacity;
    private String coverImageUrl;
    private boolean published;
    private LocalDateTime createdAt;
    private Long organizerId;
    private String organizerName;
    private String organizerProfilePictureUrl;
    private long registrationCount;
    private boolean registeredByCurrentUser;
    private boolean organizedByCurrentUser;
}
