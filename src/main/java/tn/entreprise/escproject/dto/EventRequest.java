package tn.entreprise.escproject.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventRequest {
    private String title;
    private String description;
    private String eventType;
    private String startDate;
    private String endDate;
    private String location;
    private boolean online;
    private String meetingLink;
    private int capacity;
    private String coverImageUrl;
}
