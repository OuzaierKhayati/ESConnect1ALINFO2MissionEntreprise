package tn.entreprise.escproject.entite;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    private String email;

    @NonNull
    @Column(unique=true, nullable = false)
    private String password;

    @NonNull
    private String firstName;

    @NonNull
    private String lastName;

    @JsonFormat(pattern = "MM/dd/yyyy")
    private LocalDate dateOfBirth;

    @NonNull
    @Enumerated(EnumType.STRING)
    private RoleUser roleUser;

    // Recruiter → JobOffers
    // @OneToMany(mappedBy = "recruiter")
    // private List<JobOffer> jobOffers;

    // Student → Applications
    // @OneToMany(mappedBy = "student")
    // private List<Application> applications;
}
