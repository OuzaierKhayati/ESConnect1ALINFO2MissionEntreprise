package tn.entreprise.escproject.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for facial profile enrollment request.
 * Client sends an array of face embeddings (from face-api.js).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FacialEnrollmentRequest {

    /**
     * Array of base64-encoded face embeddings.
     * Each embedding is a 128-dimensional float vector.
     * Should contain 3-5 samples for good accuracy.
     */
    @NotNull(message = "Embeddings array is required")
    private String[] embeddings;

    /**
     * User ID to enroll.
     * Must match the authenticated user's ID.
     */
    @NotNull(message = "User ID is required")
    private Long userId;

    /**
     * Optional: User consent for facial data storage.
     */
    private Boolean consentGiven = true;
}
