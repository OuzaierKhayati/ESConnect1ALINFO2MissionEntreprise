package tn.entreprise.escproject.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.entreprise.escproject.dto.ApiResponse;
import tn.entreprise.escproject.dto.EventDTO;
import tn.entreprise.escproject.services.EventService;

import java.util.List;

@RestController
@RequestMapping("/escproject/api/events")
@CrossOrigin(origins = "*", maxAge = 3600)
public class EventController {

    @Autowired
    private EventService eventService;

    /**
     * Create a new event
     */
    @PostMapping
    public ResponseEntity<ApiResponse<EventDTO>> createEvent(@RequestBody EventDTO eventDTO,
                                                             @RequestParam Long userId) {
        EventDTO createdEvent = eventService.createEvent(userId, eventDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Événement créé avec succès", createdEvent));
    }

    /**
     * Get events by creator
     */
    @GetMapping("/creator/{userId}")
    public ResponseEntity<ApiResponse<List<EventDTO>>> getEventsByCreator(@PathVariable Long userId) {
        List<EventDTO> events = eventService.getEventsByCreator(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Événements récupérés", events));
    }

    /**
     * Get upcoming events
     */
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<EventDTO>>> getUpcomingEvents() {
        List<EventDTO> events = eventService.getUpcomingEvents();
        return ResponseEntity.ok(new ApiResponse<>(true, "Événements à venir", events));
    }

    /**
     * Get public events
     */
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<EventDTO>>> getPublicEvents() {
        List<EventDTO> events = eventService.getPublicEvents();
        return ResponseEntity.ok(new ApiResponse<>(true, "Événements publics", events));
    }

    /**
     * Get event by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventDTO>> getEventById(@PathVariable Long id) {
        EventDTO event = eventService.getEventById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Événement trouvé", event));
    }

    /**
     * Update event
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventDTO>> updateEvent(@PathVariable Long id,
                                                             @RequestBody EventDTO eventDTO,
                                                             @RequestParam Long userId) {
        EventDTO updatedEvent = eventService.updateEvent(id, userId, eventDTO);
        return ResponseEntity.ok(new ApiResponse<>(true, "Événement mis à jour", updatedEvent));
    }

    /**
     * Delete event
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable Long id,
                                                         @RequestParam Long userId) {
        eventService.deleteEvent(id, userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Événement supprimé", null));
    }

    /**
     * Search events
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<EventDTO>>> searchEvents(@RequestParam String keyword) {
        List<EventDTO> events = eventService.searchEvents(keyword);
        return ResponseEntity.ok(new ApiResponse<>(true, "Résultats de recherche", events));
    }
}
