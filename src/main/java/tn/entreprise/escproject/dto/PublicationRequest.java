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
public class PublicationRequest {

    @NotBlank(message = "Le contenu ne peut pas etre vide")
    private String contenu;

    private String imageUrl;

    @NotNull(message = "L'ID de l'auteur est requis")
    private Long auteurId;
}