package tn.entreprise.escproject.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.entreprise.escproject.entite.EvaluationFormation;

import java.util.List;

public interface EvaluationFormationRepository extends JpaRepository<EvaluationFormation, Long> {

    List<EvaluationFormation> findByFormationId(Long formationId);

    boolean existsByUser_IdAndFormation_Id(Long userId, Long formationId);

    void deleteByFormationId(Long formationId);

    @Query("SELECT AVG(e.note) FROM EvaluationFormation e WHERE e.formation.id = :formationId")
    Double findAverageNoteByFormationId(@Param("formationId") Long formationId);
}