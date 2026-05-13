package tn.entreprise.escproject.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplicationResponse {
    private Long id;
    private LocalDateTime applicationDate;
    private String coverLetter;
    private String cvUrl;
    private String status;

    // Student info
    private Long studentId;
    private String studentName;
    private String studentEmail;

    // Job info
    private Long jobOfferId;
    private String jobTitle;
    private String jobCompany;
}
