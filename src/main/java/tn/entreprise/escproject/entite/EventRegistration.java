package tn.entreprise.escproject.entite;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "event_registrations",
    uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "attendee_id"})
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id")
    @JsonIgnore
    private Event event;

    @ManyToOne
    @JoinColumn(name = "attendee_id")
    @JsonIgnore
    private User attendee;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registeredAt;
}
