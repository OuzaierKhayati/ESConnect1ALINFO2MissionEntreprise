package tn.entreprise.escproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationRequest {

    @NotBlank(message = "Cover letter is required")
    @Size(min = 100, message = "Cover letter must contain at least 100 characters")
    private String coverLetter;

    @NotBlank(message = "CV URL is required")
    @Pattern(
        regexp = "^https?://.*",
        message = "Please provide a valid CV URL starting with https:// or http://"
    )
    private String cvUrl;
}
