package tn.entreprise.escproject.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import tn.entreprise.escproject.entite.Event;
import tn.entreprise.escproject.entite.EventRegistration;
import tn.entreprise.escproject.entite.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRegistrationRepository extends CrudRepository<EventRegistration, Long> {
    Optional<EventRegistration> findByEventAndAttendee(Event event, User attendee);
    boolean existsByEventAndAttendee(Event event, User attendee);
    List<EventRegistration> findByAttendee(User attendee);
    List<EventRegistration> findByEvent(Event event);
    long countByEvent(Event event);
}
