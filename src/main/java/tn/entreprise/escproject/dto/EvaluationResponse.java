package tn.entreprise.escproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EvaluationResponse {

    private Long id;
    private Integer note;
    private String commentaire;
    private String dateEvaluation;

    // User
    private Long userId;
    private String userNom;

    // Formation
    private Long formationId;
    private String formationTitre;
}