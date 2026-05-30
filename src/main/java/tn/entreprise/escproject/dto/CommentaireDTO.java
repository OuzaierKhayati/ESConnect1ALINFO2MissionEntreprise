package tn.entreprise.escproject.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentaireDTO {

    private Long id;
    private String contenu;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;

    private Long auteurId;
    private String auteurFirstName;
    private String auteurLastName;

    private Long publicationId;
}