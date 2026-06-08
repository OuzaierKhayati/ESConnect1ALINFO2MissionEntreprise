package tn.entreprise.escproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.entreprise.escproject.entite.ModeFormation;
import tn.entreprise.escproject.entite.NiveauFormation;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FormationResponse {

    private Long id;
    private String titre;
    private String description;
    private String date;
    private String heure;
    private String duree;
    private ModeFormation mode;
    private String emplacement;
    private Integer capaciteMax;
    private Double prix;
    private Boolean gratuite;
    private String image;
    private String lienReunion;
    private String categorie;
    private NiveauFormation niveau;
    private Boolean publiee;
    private Long createurId;
    private String createurNom;
    private long placesRestantes;
    private Double moyenneNote;
    private List<String> softSkills;
}