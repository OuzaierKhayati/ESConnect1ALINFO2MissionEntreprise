package tn.entreprise.escproject.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminCreateUserRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(
        regexp = "^(?=(.*\\d){2,})(?=(.*[!@#$%^&*()\\-_=+\\[\\]{};:'\",.<>?/\\\\|`~]){2,}).{8,}$",
        message = "Password must contain at least 2 numbers and 2 special characters"
    )
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @JsonFormat(pattern = "MM/dd/yyyy")
    private LocalDate dateOfBirth;

    @NotNull(message = "Role is required")
    private String roleUser;

    private String userStatus;
}
