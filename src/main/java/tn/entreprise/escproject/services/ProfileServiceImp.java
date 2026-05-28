package tn.entreprise.escproject.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import tn.entreprise.escproject.dto.*;
import tn.entreprise.escproject.entite.*;
import tn.entreprise.escproject.exception.BadRequestException;
import tn.entreprise.escproject.exception.ResourceNotFoundException;
import tn.entreprise.escproject.repositories.*;
import tn.entreprise.escproject.services.Interfaces.IProfileService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfileServiceImp implements IProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileServiceImp.class);

    private static final String UPLOAD_DIR = "uploads/profile/";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private EducationRepository educationRepository;

    @Autowired
    private ExperienceRepository experienceRepository;

    @Autowired
    private CertificationRepository certificationRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private ProjectRepository projectRepository;

    // ===== Profile =====

    @Override
    public ProfileResponse getProfileByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return buildProfileResponse(user);
    }

    @Override
    public ProfileResponse getMyProfile(String email) {
        User user = getUserByEmail(email);
        return buildProfileResponse(user);
    }

    @Override
    public ProfileResponse updateProfile(String email, ProfileUpdateRequest request) {
        User user = getUserByEmail(email);
        UserProfile profile = getOrCreateProfile(user);

        profile.setHeadline(request.getHeadline());
        profile.setAbout(request.getAbout());
        profile.setLocation(request.getLocation());
        profile.setUniversity(request.getUniversity());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setLinkedinUrl(request.getLinkedinUrl());
        profile.setGithubUrl(request.getGithubUrl());
        profile.setPortfolioUrl(request.getPortfolioUrl());
        profile.setSkills(request.getSkills());
        profile.setLanguages(request.getLanguages());

        userProfileRepository.save(profile);
        log.info("Profile updated for user: {}", email);
        return buildProfileResponse(user);
    }

    @Override
    public String uploadProfilePicture(String email, MultipartFile file) throws IOException {
        validateImageType(file);
        User user = getUserByEmail(email);
        UserProfile profile = getOrCreateProfile(user);
        deleteOldFile(profile.getProfilePictureUrl());
        String url = uploadFile(file, "avatars");
        profile.setProfilePictureUrl(url);
        profile.setProfilePicturePositionX(50); // default center
        profile.setProfilePicturePositionY(50); // default center
        userProfileRepository.save(profile);
        log.info("Profile picture updated for user: {}", email);
        return url;
    }

    @Override
    public void updateProfilePicturePosition(String email, Integer positionX, Integer positionY) {
        User user = getUserByEmail(email);
        UserProfile profile = getOrCreateProfile(user);
        profile.setProfilePicturePositionX(positionX);
        profile.setProfilePicturePositionY(positionY);
        userProfileRepository.save(profile);
        log.info("Profile picture position updated for user: {}", email);
    }

    @Override
    public String uploadCoverImage(String email, MultipartFile file) throws IOException {
        validateImageType(file);
        User user = getUserByEmail(email);
        UserProfile profile = getOrCreateProfile(user);
        deleteOldFile(profile.getCoverImageUrl());
        String url = uploadFile(file, "covers");
        profile.setCoverImageUrl(url);
        profile.setCoverPositionY(50); // default center
        userProfileRepository.save(profile);
        log.info("Cover image updated for user: {}", email);
        return url;
    }

    public void updateCoverPosition(String email, Integer positionY) {
        User user = getUserByEmail(email);
        UserProfile profile = getOrCreateProfile(user);
        profile.setCoverPositionY(positionY);
        userProfileRepository.save(profile);
        log.info("Cover position updated for user: {}", email);
    }

    @Override
    public String uploadResume(String email, MultipartFile file) throws IOException {
        User user = getUserByEmail(email);
        UserProfile profile = getOrCreateProfile(user);
        
        // Validate file type
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase() : "";
        if (!extension.matches("pdf|doc|docx|png|jpg|jpeg")) {
            throw new BadRequestException("Invalid file type. Allowed: PDF, DOC, DOCX, PNG, JPG, JPEG");
        }
        
        deleteOldFile(profile.getResumeUrl());
        String url = uploadFile(file, "resumes");
        profile.setResumeUrl(url);
        profile.setResumeFileName(originalFilename);
        userProfileRepository.save(profile);
        log.info("Resume uploaded for user: {}", email);
        return url;
    }

    // ===== Education =====

    @Override
    public EducationDTO addEducation(String email, EducationDTO dto) {
        User user = getUserByEmail(email);
        Education education = new Education();
        education.setSchool(dto.getSchool());
        education.setDegree(dto.getDegree());
        education.setFieldOfStudy(dto.getFieldOfStudy());
        education.setStartDate(dto.getStartDate());
        education.setEndDate(dto.getEndDate());
        education.setDescription(dto.getDescription());
        education.setUser(user);
        education = educationRepository.save(education);
        log.info("Education added for user: {}", email);
        return toEducationDTO(education);
    }

    @Override
    public EducationDTO updateEducation(String email, Long educationId, EducationDTO dto) {
        User user = getUserByEmail(email);
        Education education = educationRepository.findById(educationId)
                .orElseThrow(() -> new ResourceNotFoundException("Education not found with id: " + educationId));
        validateOwnership(education.getUser().getId(), user.getId());

        education.setSchool(dto.getSchool());
        education.setDegree(dto.getDegree());
        education.setFieldOfStudy(dto.getFieldOfStudy());
        education.setStartDate(dto.getStartDate());
        education.setEndDate(dto.getEndDate());
        education.setDescription(dto.getDescription());
        education = educationRepository.save(education);
        log.info("Education {} updated for user: {}", educationId, email);
        return toEducationDTO(education);
    }

    @Override
    public void deleteEducation(String email, Long educationId) {
        User user = getUserByEmail(email);
        Education education = educationRepository.findById(educationId)
                .orElseThrow(() -> new ResourceNotFoundException("Education not found with id: " + educationId));
        validateOwnership(education.getUser().getId(), user.getId());
        educationRepository.delete(education);
        log.info("Education {} deleted for user: {}", educationId, email);
    }

    // ===== Experience =====

    @Override
    public ExperienceDTO addExperience(String email, ExperienceDTO dto) {
        User user = getUserByEmail(email);
        Experience experience = new Experience();
        experience.setTitle(dto.getTitle());
        experience.setCompany(dto.getCompany());
        experience.setType(dto.getType() != null ? ExperienceType.valueOf(dto.getType()) : null);
        experience.setLocation(dto.getLocation());
        experience.setStartDate(dto.getStartDate());
        experience.setEndDate(dto.getEndDate());
        experience.setCurrentlyWorking(dto.isCurrentlyWorking());
        experience.setDescription(dto.getDescription());
        experience.setUser(user);
        experience = experienceRepository.save(experience);
        log.info("Experience added for user: {}", email);
        return toExperienceDTO(experience);
    }

    @Override
    public ExperienceDTO updateExperience(String email, Long experienceId, ExperienceDTO dto) {
        User user = getUserByEmail(email);
        Experience experience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new ResourceNotFoundException("Experience not found with id: " + experienceId));
        validateOwnership(experience.getUser().getId(), user.getId());

        experience.setTitle(dto.getTitle());
        experience.setCompany(dto.getCompany());
        experience.setType(dto.getType() != null ? ExperienceType.valueOf(dto.getType()) : null);
        experience.setLocation(dto.getLocation());
        experience.setStartDate(dto.getStartDate());
        experience.setEndDate(dto.getEndDate());
        experience.setCurrentlyWorking(dto.isCurrentlyWorking());
        experience.setDescription(dto.getDescription());
        experience = experienceRepository.save(experience);
        log.info("Experience {} updated for user: {}", experienceId, email);
        return toExperienceDTO(experience);
    }

    @Override
    public void deleteExperience(String email, Long experienceId) {
        User user = getUserByEmail(email);
        Experience experience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new ResourceNotFoundException("Experience not found with id: " + experienceId));
        validateOwnership(experience.getUser().getId(), user.getId());
        experienceRepository.delete(experience);
        log.info("Experience {} deleted for user: {}", experienceId, email);
    }

    // ===== Certification =====

    @Override
    public CertificationDTO addCertification(String email, CertificationDTO dto) {
        User user = getUserByEmail(email);
        Certification certification = new Certification();
        certification.setName(dto.getName());
        certification.setOrganization(dto.getOrganization());
        certification.setIssueDate(dto.getIssueDate());
        certification.setExpirationDate(dto.getExpirationDate());
        certification.setCredentialId(dto.getCredentialId());
        certification.setCredentialUrl(dto.getCredentialUrl());
        certification.setUser(user);
        certification = certificationRepository.save(certification);
        log.info("Certification added for user: {}", email);
        return toCertificationDTO(certification);
    }

    @Override
    public CertificationDTO updateCertification(String email, Long certificationId, CertificationDTO dto) {
        User user = getUserByEmail(email);
        Certification certification = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Certification not found with id: " + certificationId));
        validateOwnership(certification.getUser().getId(), user.getId());

        certification.setName(dto.getName());
        certification.setOrganization(dto.getOrganization());
        certification.setIssueDate(dto.getIssueDate());
        certification.setExpirationDate(dto.getExpirationDate());
        certification.setCredentialId(dto.getCredentialId());
        certification.setCredentialUrl(dto.getCredentialUrl());
        certification = certificationRepository.save(certification);
        log.info("Certification {} updated for user: {}", certificationId, email);
        return toCertificationDTO(certification);
    }

    @Override
    public void deleteCertification(String email, Long certificationId) {
        User user = getUserByEmail(email);
        Certification certification = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Certification not found with id: " + certificationId));
        validateOwnership(certification.getUser().getId(), user.getId());
        certificationRepository.delete(certification);
        log.info("Certification {} deleted for user: {}", certificationId, email);
    }

    // ===== Club =====

    @Override
    public ClubDTO addClub(String email, ClubDTO dto) {
        User user = getUserByEmail(email);
        Club club = new Club();
        club.setName(dto.getName());
        club.setRole(dto.getRole());
        club.setStartDate(dto.getStartDate());
        club.setEndDate(dto.getEndDate());
        club.setDescription(dto.getDescription());
        club.setUser(user);
        club = clubRepository.save(club);
        log.info("Club added for user: {}", email);
        return toClubDTO(club);
    }

    @Override
    public ClubDTO updateClub(String email, Long clubId, ClubDTO dto) {
        User user = getUserByEmail(email);
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found with id: " + clubId));
        validateOwnership(club.getUser().getId(), user.getId());

        club.setName(dto.getName());
        club.setRole(dto.getRole());
        club.setStartDate(dto.getStartDate());
        club.setEndDate(dto.getEndDate());
        club.setDescription(dto.getDescription());
        club = clubRepository.save(club);
        log.info("Club {} updated for user: {}", clubId, email);
        return toClubDTO(club);
    }

    @Override
    public void deleteClub(String email, Long clubId) {
        User user = getUserByEmail(email);
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found with id: " + clubId));
        validateOwnership(club.getUser().getId(), user.getId());
        clubRepository.delete(club);
        log.info("Club {} deleted for user: {}", clubId, email);
    }

    // ===== Project =====

    @Override
    public ProjectDTO addProject(String email, ProjectDTO dto) {
        User user = getUserByEmail(email);
        Project project = new Project();
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setProjectUrl(dto.getProjectUrl());
        project.setStartDate(dto.getStartDate());
        project.setEndDate(dto.getEndDate());
        project.setTechnologies(dto.getTechnologies());
        project.setUser(user);
        project = projectRepository.save(project);
        log.info("Project added for user: {}", email);
        return toProjectDTO(project);
    }

    @Override
    public ProjectDTO updateProject(String email, Long projectId, ProjectDTO dto) {
        User user = getUserByEmail(email);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        validateOwnership(project.getUser().getId(), user.getId());

        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setProjectUrl(dto.getProjectUrl());
        project.setStartDate(dto.getStartDate());
        project.setEndDate(dto.getEndDate());
        project.setTechnologies(dto.getTechnologies());
        project = projectRepository.save(project);
        log.info("Project {} updated for user: {}", projectId, email);
        return toProjectDTO(project);
    }

    @Override
    public void deleteProject(String email, Long projectId) {
        User user = getUserByEmail(email);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        validateOwnership(project.getUser().getId(), user.getId());
        projectRepository.delete(project);
        log.info("Project {} deleted for user: {}", projectId, email);
    }

    // ===== Helpers =====

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private UserProfile getOrCreateProfile(User user) {
        return userProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserProfile profile = new UserProfile();
                    profile.setUser(user);
                    return userProfileRepository.save(profile);
                });
    }

    private void validateOwnership(Long ownerId, Long userId) {
        if (!ownerId.equals(userId)) {
            throw new BadRequestException("You do not have permission to modify this resource");
        }
    }

    private String uploadFile(MultipartFile file, String subfolder) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path path = Paths.get(UPLOAD_DIR + subfolder + "/" + fileName);
        Files.createDirectories(path.getParent());
        Files.write(path, file.getBytes());
        return "/" + path.toString().replace("\\", "/");
    }

    private void validateImageType(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase() : "";
        if (!extension.matches("png|jpg|jpeg")) {
            throw new BadRequestException("Invalid image type. Allowed: PNG, JPG, JPEG");
        }
    }

    private void deleteOldFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;
        try {
            // fileUrl is like /uploads/profile/avatars/12345_filename.jpg
            String relativePath = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
            Path filePath = Paths.get(relativePath);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Could not delete old file: {}", fileUrl, e);
        }
    }

    private ProfileResponse buildProfileResponse(User user) {
        UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElse(null);
        List<Education> educations = educationRepository.findByUserIdOrderByStartDateDesc(user.getId());
        List<Experience> experiences = experienceRepository.findByUserIdOrderByStartDateDesc(user.getId());
        List<Certification> certifications = certificationRepository.findByUserIdOrderByIssueDateDesc(user.getId());
        List<Club> clubs = clubRepository.findByUserIdOrderByStartDateDesc(user.getId());
        List<Project> projects = projectRepository.findByUserIdOrderByStartDateDesc(user.getId());

        ProfileResponse response = new ProfileResponse();
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setDateOfBirth(user.getDateOfBirth());
        response.setRoleUser(user.getRoleUser().toString());
        response.setOnline(user.isOnline());

        if (profile != null) {
            response.setHeadline(profile.getHeadline());
            response.setAbout(profile.getAbout());
            response.setLocation(profile.getLocation());
            response.setUniversity(profile.getUniversity());
            response.setProfilePictureUrl(profile.getProfilePictureUrl());
            response.setProfilePicturePositionX(profile.getProfilePicturePositionX());
            response.setProfilePicturePositionY(profile.getProfilePicturePositionY());
            response.setCoverImageUrl(profile.getCoverImageUrl());
            response.setCoverPositionY(profile.getCoverPositionY());
            response.setResumeUrl(profile.getResumeUrl());
            response.setResumeFileName(profile.getResumeFileName());
            response.setLinkedinUrl(profile.getLinkedinUrl());
            response.setGithubUrl(profile.getGithubUrl());
            response.setPortfolioUrl(profile.getPortfolioUrl());
            response.setPhoneNumber(profile.getPhoneNumber());
            response.setSkills(profile.getSkills());
            response.setLanguages(profile.getLanguages());
        }

        response.setEducations(educations.stream().map(this::toEducationDTO).collect(Collectors.toList()));
        response.setExperiences(experiences.stream().map(this::toExperienceDTO).collect(Collectors.toList()));
        response.setCertifications(certifications.stream().map(this::toCertificationDTO).collect(Collectors.toList()));
        response.setClubs(clubs.stream().map(this::toClubDTO).collect(Collectors.toList()));
        response.setProjects(projects.stream().map(this::toProjectDTO).collect(Collectors.toList()));

        response.setProfileCompletionPercentage(calculateCompletion(response));

        return response;
    }

    private int calculateCompletion(ProfileResponse profile) {
        int total = 0;
        int filled = 0;

        // Basic info (5 fields)
        total += 5;
        if (profile.getHeadline() != null && !profile.getHeadline().isBlank()) filled++;
        if (profile.getAbout() != null && !profile.getAbout().isBlank()) filled++;
        if (profile.getLocation() != null && !profile.getLocation().isBlank()) filled++;
        if (profile.getUniversity() != null && !profile.getUniversity().isBlank()) filled++;
        if (profile.getProfilePictureUrl() != null && !profile.getProfilePictureUrl().isBlank()) filled++;

        // Sections (5 sections)
        total += 5;
        if (profile.getEducations() != null && !profile.getEducations().isEmpty()) filled++;
        if (profile.getExperiences() != null && !profile.getExperiences().isEmpty()) filled++;
        if (profile.getCertifications() != null && !profile.getCertifications().isEmpty()) filled++;
        if (profile.getSkills() != null && !profile.getSkills().isBlank()) filled++;
        if (profile.getProjects() != null && !profile.getProjects().isEmpty()) filled++;

        return (int) Math.round((double) filled / total * 100);
    }

    // ===== DTO Mappers =====

    private EducationDTO toEducationDTO(Education e) {
        return new EducationDTO(e.getId(), e.getSchool(), e.getDegree(), e.getFieldOfStudy(),
                e.getStartDate(), e.getEndDate(), e.getDescription());
    }

    private ExperienceDTO toExperienceDTO(Experience e) {
        return new ExperienceDTO(e.getId(), e.getTitle(), e.getCompany(),
                e.getType() != null ? e.getType().toString() : null,
                e.getLocation(), e.getStartDate(), e.getEndDate(), e.isCurrentlyWorking(), e.getDescription());
    }

    private CertificationDTO toCertificationDTO(Certification c) {
        return new CertificationDTO(c.getId(), c.getName(), c.getOrganization(),
                c.getIssueDate(), c.getExpirationDate(), c.getCredentialId(), c.getCredentialUrl());
    }

    private ClubDTO toClubDTO(Club c) {
        return new ClubDTO(c.getId(), c.getName(), c.getRole(),
                c.getStartDate(), c.getEndDate(), c.getDescription());
    }

    private ProjectDTO toProjectDTO(Project p) {
        return new ProjectDTO(p.getId(), p.getName(), p.getDescription(),
                p.getProjectUrl(), p.getStartDate(), p.getEndDate(), p.getTechnologies());
    }
}
