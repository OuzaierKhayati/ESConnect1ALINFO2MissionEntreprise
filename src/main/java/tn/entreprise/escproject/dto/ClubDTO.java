package tn.entreprise.escproject.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClubDTO {

    private Long id;

    @NotBlank(message = "Club name is required")
    private String name;

    @NotBlank(message = "Role is required")
    private String role;

    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
}
