package tn.entreprise.escproject.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.entreprise.escproject.entite.Formation;
import java.util.List;
import java.util.Optional;

public interface FormationRepository extends JpaRepository<Formation, Long> {

    @Query("SELECT DISTINCT f FROM Formation f LEFT JOIN FETCH f.softSkills WHERE f.createur.id = :createurId")
    List<Formation> findByCreateurId(@Param("createurId") Long createurId);

    @Query("SELECT DISTINCT f FROM Formation f LEFT JOIN FETCH f.softSkills WHERE f.publiee = true")
    List<Formation> findByPublieeTrue();

    @Query("SELECT DISTINCT f FROM Formation f LEFT JOIN FETCH f.softSkills WHERE f.publiee = true AND LOWER(f.categorie) = LOWER(:categorie)")
    List<Formation> findPublishedByCategorieIgnoreCase(@Param("categorie") String categorie);

    List<Formation> findByCategorie(String categorie);

    List<Formation> findByTitreContainingIgnoreCase(String keyword);

    @Query("SELECT DISTINCT f FROM Formation f LEFT JOIN FETCH f.softSkills")
    List<Formation> findAllWithSoftSkills();

    @Query("SELECT f FROM Formation f LEFT JOIN FETCH f.softSkills WHERE f.id = :id")
    Optional<Formation> findByIdWithSoftSkills(@Param("id") Long id);
}