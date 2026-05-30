package tn.entreprise.escproject.entite;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    private String role;

    @JsonIgnore
    @OneToMany(mappedBy = "createur")
    private List<Formation> formations;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<InscriptionFormation> inscriptions;
}