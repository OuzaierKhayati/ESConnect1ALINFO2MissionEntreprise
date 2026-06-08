package tn.entreprise.escproject.entite;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Formation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    private String description;

    private LocalDate date;

    private String heure;

    private String duree;

    @Enumerated(EnumType.STRING)
    private ModeFormation mode;

    private String emplacement;

    private Integer capaciteMax;

    private Double prix;

    private Boolean gratuite;

    private String image;

    private String lienReunion;

    private String categorie;

    @Enumerated(EnumType.STRING)
    private NiveauFormation niveau;

    private Boolean publiee = false;

    @ManyToOne
    @JsonIgnoreProperties({"formations", "inscriptions"})
    private User createur;

    @ManyToMany
    @JoinTable(
            name = "formation_softskill",
            joinColumns = @JoinColumn(name = "formation_id"),
            inverseJoinColumns = @JoinColumn(name = "softskill_id")
    )
    private List<SoftSkill> softSkills;
}