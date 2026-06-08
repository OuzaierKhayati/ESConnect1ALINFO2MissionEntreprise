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
public class InscriptionFormation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dateInscription;

    @Enumerated(EnumType.STRING)
    private StatutInscription statut = StatutInscription.EN_ATTENTE;

    @ManyToOne
    @JsonIgnoreProperties({"formations", "inscriptions"})
    private User user;

    @ManyToOne
    @JsonIgnoreProperties({"softSkills", "createur"})
    private Formation formation;
}