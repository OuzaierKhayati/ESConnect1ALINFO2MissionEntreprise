package tn.entreprise.escproject.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import tn.entreprise.escproject.entite.Event;
import tn.entreprise.escproject.entite.EventType;
import tn.entreprise.escproject.entite.User;

import java.util.List;

@Repository
public interface EventRepository extends CrudRepository<Event, Long> {
    List<Event> findByPublishedTrueOrderByStartDateAsc();
    List<Event> findByOrganizerOrderByCreatedAtDesc(User organizer);
    List<Event> findByPublishedTrueAndEventTypeOrderByStartDateAsc(EventType eventType);
    List<Event> findByPublishedTrueAndTitleContainingIgnoreCaseOrderByStartDateAsc(String title);
}
