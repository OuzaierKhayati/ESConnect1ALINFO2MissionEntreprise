package tn.entreprise.escproject.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import tn.entreprise.escproject.entite.JobOffer;
import tn.entreprise.escproject.entite.JobStatus;
import tn.entreprise.escproject.entite.JobType;
import tn.entreprise.escproject.entite.User;

import java.util.List;

@Repository
public interface JobOfferRepository extends CrudRepository<JobOffer, Long> {
    List<JobOffer> findByRecruiter(User recruiter);
    List<JobOffer> findByRecruiterOrderByCreatedAtDesc(User recruiter);
    List<JobOffer> findByStatus(JobStatus status);
    List<JobOffer> findByJobType(JobType jobType);
    List<JobOffer> findAllByOrderByCreatedAtDesc();
    List<JobOffer> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);
}
