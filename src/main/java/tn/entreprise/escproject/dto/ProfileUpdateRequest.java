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
public class ProfileUpdateRequest {

    private String headline;
    private String about;
    private String location;
    private String university;
    private String phoneNumber;
    private String linkedinUrl;
    private String githubUrl;
    private String portfolioUrl;
    private String skills;
    private String languages;
}
