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
public class DemandeFormateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dateDemande;

    // EN_ATTENTE, APPROUVEE, REJETEE
    private String statut = "EN_ATTENTE";

    private String motif;

    @ManyToOne
    @JsonIgnoreProperties({"formations", "inscriptions"})
    private User user;
}