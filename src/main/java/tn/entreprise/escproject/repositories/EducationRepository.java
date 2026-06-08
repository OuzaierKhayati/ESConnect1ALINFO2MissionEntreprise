package tn.entreprise.escproject.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import tn.entreprise.escproject.entite.Education;

import java.util.List;

@Repository
public interface EducationRepository extends CrudRepository<Education, Long> {

    List<Education> findByUserIdOrderByStartDateDesc(Long userId);
}
