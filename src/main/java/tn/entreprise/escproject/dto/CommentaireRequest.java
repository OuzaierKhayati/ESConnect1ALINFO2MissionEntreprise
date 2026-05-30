package tn.entreprise.escproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CommentaireRequest {

    @NotBlank(message = "Le contenu du commentaire ne peut pas etre vide")
    private String contenu;

    @NotNull(message = "L'ID de la publication est requis")
    private Long publicationId;

    @NotNull(message = "L'ID de l'auteur est requis")
    private Long auteurId;
}