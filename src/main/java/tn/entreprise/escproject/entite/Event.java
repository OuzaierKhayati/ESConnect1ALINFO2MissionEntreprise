package tn.entreprise.escproject.entite;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 3000)
    private String description;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;

    private String location;

    private boolean online;

    private String meetingLink;

    private int capacity;

    private String coverImageUrl;

    private boolean published;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "organizer_id")
    @JsonIgnore
    private User organizer;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<EventRegistration> registrations = new ArrayList<>();
}
