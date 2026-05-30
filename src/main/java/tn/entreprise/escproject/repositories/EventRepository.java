package tn.entreprise.escproject.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.entreprise.escproject.entite.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // Find events by creator
    List<Event> findByCreateurIdOrderByDateDebutDesc(Long createurId);

    // Find upcoming events
    @Query("SELECT e FROM Event e WHERE e.dateDebut > CURRENT_TIMESTAMP ORDER BY e.dateDebut ASC")
    List<Event> findUpcomingEvents();

    // Find events by keyword
    @Query("SELECT e FROM Event e WHERE LOWER(e.titre) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY e.dateDebut DESC")
    List<Event> searchEvents(@Param("keyword") String keyword);

    // Find public events
    @Query("SELECT e FROM Event e WHERE e.isPublic = true ORDER BY e.dateDebut DESC")
    List<Event> findPublicEvents();

    // Find events by organizer in a date range
    @Query("SELECT e FROM Event e WHERE e.createur.id = :createurId " +
            "AND e.dateDebut BETWEEN :dateDebut AND :dateFin " +
            "ORDER BY e.dateDebut ASC")
    List<Event> findByCreateurAndDateRange(@Param("createurId") Long createurId,
                                           @Param("dateDebut") LocalDateTime dateDebut,
                                           @Param("dateFin") LocalDateTime dateFin);

    // Check if event exists
    Optional<Event> findById(Long id);
}
