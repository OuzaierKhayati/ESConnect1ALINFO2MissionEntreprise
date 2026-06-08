//package tn.entreprise.escproject.entite;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import jakarta.persistence.*;
//import jakarta.validation.constraints.Email;
//import jakarta.validation.constraints.NotBlank;
//import lombok.*;
//
//import java.util.List;
//
//@Entity
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//@Table(name = "users")
//public class User {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @NotBlank
//    private String firstName;
//
//    @NotBlank
//    private String lastName;
//
//    @Email
//    @Column(unique = true)
//    private String email;
//
//    @NotBlank
//    private String password;
//
//    @JsonIgnore
//    @OneToMany(mappedBy = "sender")
//    private List<Connection> sentConnections;
//
//    @JsonIgnore
//    @OneToMany(mappedBy = "receiver")
//    private List<Connection> receivedConnections;
//
//    @JsonIgnore
//    @OneToMany(mappedBy = "sender")
//    private List<Message> sentMessages;
//
//    @JsonIgnore
//    @OneToMany(mappedBy = "receiver")
//    private List<Message> receivedMessages;
//}

package tn.entreprise.escproject.entite;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @Column(unique = true)
    private String email;

    @Column(nullable = true)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider = AuthProvider.LOCAL;

    private String providerId;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @JsonFormat(pattern = "MM/dd/yyyy")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private RoleUser roleUser;

    @Enumerated(EnumType.STRING)
    private UserStatus userStatus;

    private Boolean online = false;

    public boolean isOnline() {
        return Boolean.TRUE.equals(online);
    }

    // Recruiter → JobOffers
    @JsonIgnore
    @OneToMany(mappedBy = "recruiter")
    private List<JobOffer> jobOffers;

    // Student → Applications
    @JsonIgnore
    @OneToMany(mappedBy = "student")
    private List<Application> applications;

    @JsonIgnore
    @OneToMany(mappedBy = "sender")
    private List<Connection> sentConnections;

    @JsonIgnore
    @OneToMany(mappedBy = "receiver")
    private List<Connection> receivedConnections;

    @JsonIgnore
    @OneToMany(mappedBy = "sender")
    private List<Message> sentMessages;

    @JsonIgnore
    @OneToMany(mappedBy = "receiver")
    private List<Message> receivedMessages;

    // Profile
    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserProfile profile;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Education> educations;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Experience> experiences;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Certification> certifications;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Club> clubs;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Project> projects;

    @JsonIgnore
    @OneToMany(mappedBy = "createur")
    private List<Formation> formations;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<InscriptionFormation> inscriptions;
}