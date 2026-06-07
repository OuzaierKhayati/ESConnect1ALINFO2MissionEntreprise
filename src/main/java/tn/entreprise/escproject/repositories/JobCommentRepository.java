package tn.entreprise.escproject.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import tn.entreprise.escproject.entite.JobComment;
import tn.entreprise.escproject.entite.JobOffer;

import java.util.List;

@Repository
public interface JobCommentRepository extends CrudRepository<JobComment, Long> {
    List<JobComment> findByJobOfferOrderByCreatedAtDesc(JobOffer jobOffer);
}
