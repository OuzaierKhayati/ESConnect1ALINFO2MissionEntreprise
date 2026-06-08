package tn.entreprise.escproject.entite;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applicationDate;

    private String cvUrl;

    @Column(length = 3000)
    private String coverLetter;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    // One student can apply to multiple job offers so he can have multiple applications    
    @ManyToOne
    @JoinColumn(name = "student_id")
    @JsonIgnore
    private User student;

    // One application belongs to one job offer
    @ManyToOne
    @JoinColumn(name = "job_offer_id")
    @JsonIgnore
    private JobOffer jobOffer;
}