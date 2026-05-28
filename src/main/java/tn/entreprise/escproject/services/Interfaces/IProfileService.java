package tn.entreprise.escproject.services.Interfaces;

import org.springframework.web.multipart.MultipartFile;
import tn.entreprise.escproject.dto.*;

import java.io.IOException;

public interface IProfileService {

    ProfileResponse getProfileByUserId(Long userId);

    ProfileResponse getMyProfile(String email);

    ProfileResponse updateProfile(String email, ProfileUpdateRequest request);

    String uploadProfilePicture(String email, MultipartFile file) throws IOException;

    void updateProfilePicturePosition(String email, Integer positionX, Integer positionY);

    String uploadCoverImage(String email, MultipartFile file) throws IOException;

    void updateCoverPosition(String email, Integer positionY);

    String uploadResume(String email, MultipartFile file) throws IOException;

    // Education
    EducationDTO addEducation(String email, EducationDTO dto);
    EducationDTO updateEducation(String email, Long educationId, EducationDTO dto);
    void deleteEducation(String email, Long educationId);

    // Experience
    ExperienceDTO addExperience(String email, ExperienceDTO dto);
    ExperienceDTO updateExperience(String email, Long experienceId, ExperienceDTO dto);
    void deleteExperience(String email, Long experienceId);

    // Certification
    CertificationDTO addCertification(String email, CertificationDTO dto);
    CertificationDTO updateCertification(String email, Long certificationId, CertificationDTO dto);
    void deleteCertification(String email, Long certificationId);

    // Club
    ClubDTO addClub(String email, ClubDTO dto);
    ClubDTO updateClub(String email, Long clubId, ClubDTO dto);
    void deleteClub(String email, Long clubId);

    // Project
    ProjectDTO addProject(String email, ProjectDTO dto);
    ProjectDTO updateProject(String email, Long projectId, ProjectDTO dto);
    void deleteProject(String email, Long projectId);
}
