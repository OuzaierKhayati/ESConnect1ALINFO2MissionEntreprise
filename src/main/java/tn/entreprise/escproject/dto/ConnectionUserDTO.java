package tn.entreprise.escproject.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConnectionUserDTO {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String roleUser;

    private String profilePictureUrl;
}
