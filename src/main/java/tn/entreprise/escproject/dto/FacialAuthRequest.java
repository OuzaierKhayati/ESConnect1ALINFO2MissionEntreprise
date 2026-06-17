package tn.entreprise.escproject.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for facial authentication login request.
 * Client sends a single face embedding captured during login.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FacialAuthRequest {

    /**
     * Base64-encoded face embedding captured during login.
     * Single 128-dimensional float vector from face-api.js.
     */
    @NotBlank(message = "Embedding is required")
    private String embedding;

    /**
     * Optional: Face detection confidence score.
     * Used for liveness/quality checking.
     */
    private Double confidence;
}
