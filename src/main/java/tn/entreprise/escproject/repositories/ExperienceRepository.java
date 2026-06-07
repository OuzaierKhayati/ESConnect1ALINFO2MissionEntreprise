package tn.entreprise.escproject.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import tn.entreprise.escproject.entite.Experience;

import java.util.List;

@Repository
public interface ExperienceRepository extends CrudRepository<Experience, Long> {

    List<Experience> findByUserIdOrderByStartDateDesc(Long userId);
}
