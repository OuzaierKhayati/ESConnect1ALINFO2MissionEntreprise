package tn.entreprise.escproject.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.entreprise.escproject.entite.SoftSkill;

import java.util.Optional;

public interface SoftSkillRepository extends JpaRepository<SoftSkill, Long> {

    Optional<SoftSkill> findByNom(String nom);

    boolean existsByNom(String nom);
}