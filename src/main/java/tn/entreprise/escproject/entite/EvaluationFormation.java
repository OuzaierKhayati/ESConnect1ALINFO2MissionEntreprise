package tn.entreprise.escproject.entite;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationFormation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Note de 1 à 5
    private Integer note;

    private String commentaire;

    private LocalDate dateEvaluation;

    @ManyToOne
    @JsonIgnoreProperties({"formations", "inscriptions"})
    private User user;

    @ManyToOne
    @JsonIgnoreProperties({"softSkills", "createur"})
    private Formation formation;
}