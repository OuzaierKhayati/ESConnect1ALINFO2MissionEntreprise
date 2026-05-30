package tn.entreprise.escproject.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.entreprise.escproject.entite.DemandeFormateur;

import java.util.List;
import java.util.Optional;

public interface DemandeFormateurRepository extends JpaRepository<DemandeFormateur, Long> {

    List<DemandeFormateur> findByStatut(String statut);

    Optional<DemandeFormateur> findByUser_IdAndStatut(Long userId, String statut);

    boolean existsByUser_IdAndStatut(Long userId, String statut);
}