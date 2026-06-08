package tn.entreprise.escproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.entreprise.escproject.entite.StatutInscription;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InscriptionResponse {

    private Long id;
    private String dateInscription;
    private StatutInscription statut;

    // User
    private Long userId;
    private String userNom;
    private String userEmail;

    // Formation
    private Long formationId;
    private String formationTitre;
}