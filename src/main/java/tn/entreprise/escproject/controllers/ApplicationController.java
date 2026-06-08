package tn.entreprise.escproject.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tn.entreprise.escproject.dto.ApiResponse;
import tn.entreprise.escproject.dto.ApplicationRequest;
import tn.entreprise.escproject.dto.ApplicationResponse;
import tn.entreprise.escproject.entite.User;
import tn.entreprise.escproject.exception.ResourceNotFoundException;
import tn.entreprise.escproject.repositories.UserRepository;
import tn.entreprise.escproject.services.ApplicationServiceImp;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

    @Autowired
    private ApplicationServiceImp applicationService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/job/{jobId}")
    public ResponseEntity<ApiResponse<ApplicationResponse>> applyToJob(
            @PathVariable Long jobId,
            @RequestBody ApplicationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        ApplicationResponse response = applicationService.applyToJob(jobId, request, user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Application submitted successfully", response));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getMyApplications(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        List<ApplicationResponse> applications = applicationService.getMyApplications(user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Applications retrieved", applications));
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getApplicationsForJob(
            @PathVariable Long jobId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        List<ApplicationResponse> applications = applicationService.getApplicationsForJob(jobId, user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Applications retrieved", applications));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        ApplicationResponse response = applicationService.updateApplicationStatus(id, body.get("status"), user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Application status updated", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> withdrawApplication(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        applicationService.withdrawApplication(id, user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Application withdrawn successfully", null));
    }

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
