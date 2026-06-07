package tn.entreprise.escproject.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import tn.entreprise.escproject.dto.*;
import tn.entreprise.escproject.services.ProfileServiceImp;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private ProfileServiceImp profileService;

    // ===== Profile =====

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        ProfileResponse profile = profileService.getMyProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profile));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfileByUserId(@PathVariable Long userId) {
        ProfileResponse profile = profileService.getProfileByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profile));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ProfileUpdateRequest request) {
        ProfileResponse profile = profileService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", profile));
    }

    // ===== File Uploads =====

    @PostMapping("/me/picture")
    public ResponseEntity<ApiResponse<String>> uploadProfilePicture(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) throws IOException {
        String url = profileService.uploadProfilePicture(userDetails.getUsername(), file);
        return ResponseEntity.ok(ApiResponse.success("Profile picture uploaded successfully", url));
    }

    @PostMapping("/me/cover")
    public ResponseEntity<ApiResponse<String>> uploadCoverImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) throws IOException {
        String url = profileService.uploadCoverImage(userDetails.getUsername(), file);
        return ResponseEntity.ok(ApiResponse.success("Cover image uploaded successfully", url));
    }

    @PostMapping("/me/resume")
    public ResponseEntity<ApiResponse<String>> uploadResume(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) throws IOException {
        String url = profileService.uploadResume(userDetails.getUsername(), file);
        return ResponseEntity.ok(ApiResponse.success("Resume uploaded successfully", url));
    }

    @PutMapping("/me/cover-position")
    public ResponseEntity<ApiResponse<Void>> updateCoverPosition(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("positionY") Integer positionY) {
        profileService.updateCoverPosition(userDetails.getUsername(), positionY);
        return ResponseEntity.ok(ApiResponse.success("Cover position updated", null));
    }

    @PutMapping("/me/picture-position")
    public ResponseEntity<ApiResponse<Void>> updateProfilePicturePosition(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("positionX") Integer positionX,
            @RequestParam("positionY") Integer positionY) {
        profileService.updateProfilePicturePosition(userDetails.getUsername(), positionX, positionY);
        return ResponseEntity.ok(ApiResponse.success("Profile picture position updated", null));
    }

    // ===== Education =====

    @PostMapping("/me/education")
    public ResponseEntity<ApiResponse<EducationDTO>> addEducation(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody EducationDTO dto) {
        EducationDTO result = profileService.addEducation(userDetails.getUsername(), dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Education added successfully", result));
    }

    @PutMapping("/me/education/{id}")
    public ResponseEntity<ApiResponse<EducationDTO>> updateEducation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody EducationDTO dto) {
        EducationDTO result = profileService.updateEducation(userDetails.getUsername(), id, dto);
        return ResponseEntity.ok(ApiResponse.success("Education updated successfully", result));
    }

    @DeleteMapping("/me/education/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEducation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        profileService.deleteEducation(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Education deleted successfully"));
    }

    // ===== Experience =====

    @PostMapping("/me/experience")
    public ResponseEntity<ApiResponse<ExperienceDTO>> addExperience(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ExperienceDTO dto) {
        ExperienceDTO result = profileService.addExperience(userDetails.getUsername(), dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Experience added successfully", result));
    }

    @PutMapping("/me/experience/{id}")
    public ResponseEntity<ApiResponse<ExperienceDTO>> updateExperience(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody ExperienceDTO dto) {
        ExperienceDTO result = profileService.updateExperience(userDetails.getUsername(), id, dto);
        return ResponseEntity.ok(ApiResponse.success("Experience updated successfully", result));
    }

    @DeleteMapping("/me/experience/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExperience(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        profileService.deleteExperience(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Experience deleted successfully"));
    }

    // ===== Certification =====

    @PostMapping("/me/certification")
    public ResponseEntity<ApiResponse<CertificationDTO>> addCertification(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CertificationDTO dto) {
        CertificationDTO result = profileService.addCertification(userDetails.getUsername(), dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Certification added successfully", result));
    }

    @PutMapping("/me/certification/{id}")
    public ResponseEntity<ApiResponse<CertificationDTO>> updateCertification(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody CertificationDTO dto) {
        CertificationDTO result = profileService.updateCertification(userDetails.getUsername(), id, dto);
        return ResponseEntity.ok(ApiResponse.success("Certification updated successfully", result));
    }

    @DeleteMapping("/me/certification/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCertification(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        profileService.deleteCertification(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Certification deleted successfully"));
    }

    // ===== Club =====

    @PostMapping("/me/club")
    public ResponseEntity<ApiResponse<ClubDTO>> addClub(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ClubDTO dto) {
        ClubDTO result = profileService.addClub(userDetails.getUsername(), dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Club added successfully", result));
    }

    @PutMapping("/me/club/{id}")
    public ResponseEntity<ApiResponse<ClubDTO>> updateClub(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody ClubDTO dto) {
        ClubDTO result = profileService.updateClub(userDetails.getUsername(), id, dto);
        return ResponseEntity.ok(ApiResponse.success("Club updated successfully", result));
    }

    @DeleteMapping("/me/club/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteClub(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        profileService.deleteClub(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Club deleted successfully"));
    }

    // ===== Project =====

    @PostMapping("/me/project")
    public ResponseEntity<ApiResponse<ProjectDTO>> addProject(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProjectDTO dto) {
        ProjectDTO result = profileService.addProject(userDetails.getUsername(), dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Project added successfully", result));
    }

    @PutMapping("/me/project/{id}")
    public ResponseEntity<ApiResponse<ProjectDTO>> updateProject(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody ProjectDTO dto) {
        ProjectDTO result = profileService.updateProject(userDetails.getUsername(), id, dto);
        return ResponseEntity.ok(ApiResponse.success("Project updated successfully", result));
    }

    @DeleteMapping("/me/project/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        profileService.deleteProject(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Project deleted successfully"));
    }
}
