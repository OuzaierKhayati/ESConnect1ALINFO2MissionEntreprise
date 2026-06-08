package tn.entreprise.escproject.entite;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String headline;

    @Column(length = 2000)
    private String about;

    private String location;

    private String university;

    private String profilePictureUrl;

    private Integer profilePicturePositionX; // percentage 0-100 for horizontal position

    private Integer profilePicturePositionY; // percentage 0-100 for vertical position

    private String coverImageUrl;

    private Integer coverPositionY; // percentage 0-100 for vertical position

    private String resumeUrl;

    private String resumeFileName;

    private String linkedinUrl;

    private String githubUrl;

    private String portfolioUrl;

    private String phoneNumber;

    @Column(length = 500)
    private String skills;

    @Column(length = 500)
    private String languages;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}
