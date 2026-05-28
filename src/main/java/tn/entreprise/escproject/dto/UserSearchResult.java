package tn.entreprise.escproject.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserSearchResult {

    private Long id;
    private String firstName;
    private String lastName;
    private String roleUser;
    private String headline;
    private String profilePictureUrl;
    private boolean online;
}
