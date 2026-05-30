package tn.entreprise.escproject.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.entreprise.escproject.entite.InscriptionFormation;
import tn.entreprise.escproject.entite.StatutInscription;

import java.util.List;
import java.util.Optional;

public interface InscriptionFormationRepository extends JpaRepository<InscriptionFormation, Long> {

    long countByFormationId(Long formationId);

    boolean existsByUser_IdAndFormation_Id(Long userId, Long formationId);

    List<InscriptionFormation> findByFormationId(Long formationId);

    List<InscriptionFormation> findByUserId(Long userId);

    Optional<InscriptionFormation> findByUser_IdAndFormation_Id(Long userId, Long formationId);

    List<InscriptionFormation> findByFormationIdAndStatut(Long formationId, StatutInscription statut);
}