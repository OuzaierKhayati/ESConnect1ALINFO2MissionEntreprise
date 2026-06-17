package tn.entreprise.escproject.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import tn.entreprise.escproject.dto.ApiResponse;
import tn.entreprise.escproject.dto.FacialAuthRequest;
import tn.entreprise.escproject.dto.FacialEnrollmentRequest;
import tn.entreprise.escproject.dto.LoginResponse;
import tn.entreprise.escproject.entite.FacialProfile;
import tn.entreprise.escproject.entite.User;
import tn.entreprise.escproject.exception.BadRequestException;
import tn.entreprise.escproject.exception.ResourceNotFoundException;
import tn.entreprise.escproject.exception.UnauthorizedException;
import tn.entreprise.escproject.repositories.UserRepository;
import tn.entreprise.escproject.services.FacialRecognitionService;
import tn.entreprise.escproject.utils.JwtUtil;

/**
 * Controller for facial recognition authentication endpoints.
 * Handles enrollment and login verification via facial recognition.
 */
@RestController
@RequestMapping("/facial")
public class FacialAuthController {

    private static final Logger log = LoggerFactory.getLogger(FacialAuthController.class);

    @Autowired
    private FacialRecognitionService facialRecognitionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Enroll a user's face during registration or later.
     * Can be called by authenticated users OR with userId in request body for post-registration enrollment.
     * This allows new users to enroll immediately after registration without needing to log in first.
     *
     * POST /facial/enroll
     * Body: FacialEnrollmentRequest with embeddings array and userId
     * Authorization: Optional (either JWT token or userId in body)
     *
     * @param userDetails Optional authenticated user (may be null for post-registration enrollment)
     * @param request Enrollment request with face embeddings and userId
     * @return FacialProfile enrollment confirmation
     */
    @PostMapping("/enroll")
    public ResponseEntity<ApiResponse<String>> enrollFace(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody FacialEnrollmentRequest request) {

        log.info("Facial enrollment request for userId: {}", request.getUserId());

        Long userId = null;
        
        if (userDetails != null) {
            // Authenticated user - use their ID from token
            User authenticatedUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            userId = authenticatedUser.getId();
            
            // Verify that request is for the authenticated user (security check)
            if (!authenticatedUser.getId().equals(request.getUserId())) {
                throw new BadRequestException("Cannot enroll facial profile for another user");
            }
            
            log.info("Facial enrollment from authenticated user: {}", userDetails.getUsername());
        } else if (request.getUserId() != null && request.getUserId() > 0) {
            // Post-registration enrollment - use userId from request body
            userId = request.getUserId();
            log.info("Facial enrollment for post-registration userId: {}", userId);
        } else {
            throw new BadRequestException("Either authentication token or userId in request body is required");
        }

        // Fetch user by ID
        final Long resolvedUserId = userId;
        User user = userRepository.findById(resolvedUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + resolvedUserId));

        try {
            FacialProfile facialProfile = facialRecognitionService.enrollUser(user, request);
            log.info("Facial enrollment successful for user: {}", user.getEmail());
            return ResponseEntity.ok(
                ApiResponse.success(
                    "Facial profile with " + request.getEmbeddings().length + " samples created",
                    "Enrollment successful"
                )
            );
        } catch (BadRequestException e) {
            log.warn("Facial enrollment validation error for user: {}", user.getEmail());
            throw e;
        } catch (Exception e) {
            log.error("Facial enrollment error for user: {}", user.getEmail(), e);
            throw new BadRequestException("Failed to enroll facial profile: " + e.getMessage());
        }
    }

    /**
     * Authenticate a user via facial recognition.
     * This is a public endpoint called during login.
     *
     * POST /facial/authenticate
     * Query Param: email - User's email (needed to find their facial profile)
     * Body: FacialAuthRequest with single embedding
     *
     * @param email User's email to authenticate
     * @param request Authentication request with face embedding
     * @return JWT token if verification succeeds
     */
    @PostMapping("/authenticate")
    public ResponseEntity<ApiResponse<LoginResponse>> authenticateWithFace(
            @RequestParam String email,
            @Valid @RequestBody FacialAuthRequest request) {

        log.info("Facial authentication attempt for email: {}", email);

        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException("Email is required for facial authentication");
        }

        User user = userRepository.findByEmail(email.trim())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // Check if user has active facial profile
        if (!facialRecognitionService.hasActiveFacialProfile(user.getId())) {
            log.warn("User {} has no active facial profile", email);
            throw new BadRequestException("Facial profile not found. Please enroll your face first.");
        }

        // Check if user account is active
        if (!user.isOnline() && user.getUserStatus() != null) {
            // Allow verification even if offline (they're trying to log in)
        }

        try {
            // Verify facial embedding
            boolean isVerified = facialRecognitionService.verifyFace(user, request);

            if (!isVerified) {
                log.warn("Facial verification failed for user: {}", email);
                throw new UnauthorizedException("Face not recognized. Please try again.");
            }

            // Generate JWT token (same as password login)
            String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRoleUser().toString());

            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setType("Bearer");
            response.setId(user.getId());
            response.setEmail(user.getEmail());
            response.setFirstName(user.getFirstName());
            response.setLastName(user.getLastName());
            response.setRoleUser(user.getRoleUser().toString());

            log.info("Facial authentication successful for user: {}", email);
            return ResponseEntity.ok(
                ApiResponse.success("Authentication successful", response)
            );

        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during facial authentication for user: {}", email, e);
            throw new BadRequestException("Facial authentication failed: " + e.getMessage());
        }
    }

    /**
     * Delete a user's facial profile.
     * User can disable facial authentication without deleting the entire profile.
     * Requires authentication.
     *
     * DELETE /facial/profile
     *
     * @param userDetails Authenticated user details
     * @return Confirmation message
     */
    @DeleteMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> deleteFacialProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            throw new UnauthorizedException("User must be logged in to delete facial profile");
        }

        log.info("Delete facial profile request from user: {}", userDetails.getUsername());

        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!facialRecognitionService.hasActiveFacialProfile(user.getId())) {
            throw new ResourceNotFoundException("No facial profile found to delete");
        }

        try {
            facialRecognitionService.deleteFacialProfile(user);
            log.info("Facial profile deleted for user: {}", user.getEmail());
            return ResponseEntity.ok(ApiResponse.success("Facial profile deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting facial profile for user: {}", user.getEmail(), e);
            throw new BadRequestException("Failed to delete facial profile: " + e.getMessage());
        }
    }

    /**
     * Check whether the authenticated user has an active facial profile.
     * Used by profile settings to render facial authentication toggle state.
     *
     * GET /facial/me/status
     *
     * @param userDetails Authenticated user details
     * @return true if current user has active facial profile
     */
    @GetMapping("/me/status")
    public ResponseEntity<ApiResponse<Boolean>> checkMyFacialProfileStatus(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            throw new UnauthorizedException("User must be logged in to check facial profile status");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean hasFacialProfile = facialRecognitionService.hasActiveFacialProfile(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Facial profile status", hasFacialProfile));
    }

    /**
     * Check if user has an active facial profile.
     * Public endpoint - useful for UI to show "Login with Face" button.
     *
     * GET /facial/check?email=user@example.com
     *
     * @param email User's email
     * @return true if user has active facial profile
     */
    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Boolean>> checkFacialProfile(@RequestParam String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException("Email is required");
        }

        User user = userRepository.findByEmail(email.trim())
            .orElse(null);

        if (user == null) {
            return ResponseEntity.ok(ApiResponse.success("Facial profile status", false));
        }

        boolean hasFacialProfile = facialRecognitionService.hasActiveFacialProfile(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Facial profile status", hasFacialProfile));
    }

    /**
     * Re-activate a previously disabled facial profile.
     * Requires authentication.
     *
     * POST /facial/reactivate
     *
     * @param userDetails Authenticated user details
     * @return Confirmation message
     */
    @PostMapping("/reactivate")
    public ResponseEntity<ApiResponse<Void>> reactivateFacialProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            throw new UnauthorizedException("User must be logged in to reactivate facial profile");
        }

        log.info("Reactivate facial profile request from user: {}", userDetails.getUsername());

        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        try {
            facialRecognitionService.reactivateFacialProfile(user);
            log.info("Facial profile reactivated for user: {}", user.getEmail());
            return ResponseEntity.ok(ApiResponse.success("Facial profile reactivated successfully"));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error reactivating facial profile for user: {}", user.getEmail(), e);
            throw new BadRequestException("Failed to reactivate facial profile: " + e.getMessage());
        }
    }
}
