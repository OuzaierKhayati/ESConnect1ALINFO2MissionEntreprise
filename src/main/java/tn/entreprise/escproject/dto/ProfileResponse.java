package tn.entreprise.escproject.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponse {

    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String roleUser;
    private boolean online;

    // Profile info
    private String headline;
    private String about;
    private String location;
    private String university;
    private String profilePictureUrl;
    private Integer profilePicturePositionX;
    private Integer profilePicturePositionY;
    private String coverImageUrl;
    private Integer coverPositionY;
    private String resumeUrl;
    private String resumeFileName;
    private String linkedinUrl;
    private String githubUrl;
    private String portfolioUrl;
    private String phoneNumber;
    private String skills;
    private String languages;

    // Sub-sections
    private List<EducationDTO> educations;
    private List<ExperienceDTO> experiences;
    private List<CertificationDTO> certifications;
    private List<ClubDTO> clubs;
    private List<ProjectDTO> projects;

    // Computed
    private int profileCompletionPercentage;
}
