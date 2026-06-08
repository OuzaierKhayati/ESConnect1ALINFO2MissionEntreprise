package tn.entreprise.escproject.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import tn.entreprise.escproject.entite.Application;
import tn.entreprise.escproject.entite.JobOffer;
import tn.entreprise.escproject.entite.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends CrudRepository<Application, Long> {
    List<Application> findByStudent(User student);
    List<Application> findByJobOffer(JobOffer jobOffer);
    Optional<Application> findByStudentAndJobOffer(User student, JobOffer jobOffer);
    boolean existsByStudentAndJobOffer(User student, JobOffer jobOffer);
}
