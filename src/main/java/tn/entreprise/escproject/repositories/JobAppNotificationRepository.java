package tn.entreprise.escproject.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import tn.entreprise.escproject.entite.JobAppNotification;
import tn.entreprise.escproject.entite.User;

import java.util.List;

@Repository
public interface JobAppNotificationRepository extends CrudRepository<JobAppNotification, Long> {
    List<JobAppNotification> findByRecipientOrderByCreatedAtDesc(User recipient);
    List<JobAppNotification> findByRecipientAndReadFalseOrderByCreatedAtDesc(User recipient);
    long countByRecipientAndReadFalse(User recipient);
}
