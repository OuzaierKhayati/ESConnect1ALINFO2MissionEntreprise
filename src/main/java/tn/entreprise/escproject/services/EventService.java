package tn.entreprise.escproject.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.entreprise.escproject.dto.EventDTO;
import tn.entreprise.escproject.entite.Event;
import tn.entreprise.escproject.entite.EventStatus;
import tn.entreprise.escproject.entite.User;
import tn.entreprise.escproject.exception.BadRequestException;
import tn.entreprise.escproject.repositories.EventRepository;
import tn.entreprise.escproject.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new event
     */
    public EventDTO createEvent(Long createurId, EventDTO eventDTO) {
        if (eventDTO.getTitre() == null || eventDTO.getTitre().trim().isEmpty()) {
            throw new BadRequestException("Le titre de l'événement est requis");
        }

        if (eventDTO.getDateDebut() == null || eventDTO.getDateFin() == null) {
            throw new BadRequestException("Les dates de début et fin sont requises");
        }

        if (eventDTO.getDateDebut().isAfter(eventDTO.getDateFin())) {
            throw new BadRequestException("La date de début doit être avant la date de fin");
        }

        Optional<User> creator = userRepository.findById(createurId);
        if (creator.isEmpty()) {
            throw new BadRequestException("L'utilisateur créateur n'existe pas");
        }

        Event event = new Event();
        event.setTitre(eventDTO.getTitre());
        event.setDescription(eventDTO.getDescription() != null ? eventDTO.getDescription() : "");
        event.setDateDebut(eventDTO.getDateDebut());
        event.setDateFin(eventDTO.getDateFin());
        event.setLocalisation(eventDTO.getLocalisation() != null ? eventDTO.getLocalisation() : "");
        event.setImageUrl(eventDTO.getImageUrl());
        event.setCreateur(creator.get());
        event.setIsPublic(eventDTO.getIsPublic() != null ? eventDTO.getIsPublic() : true);
        event.setStatut(EventStatus.PLANIFIE);
        event.setNombreParticipants(1); // Creator is the first participant

        Event savedEvent = eventRepository.save(event);
        return mapEventToDTO(savedEvent);
    }

    /**
     * Get all events by creator
     */
    public List<EventDTO> getEventsByCreator(Long createurId) {
        List<Event> events = eventRepository.findByCreateurIdOrderByDateDebutDesc(createurId);
        return events.stream().map(this::mapEventToDTO).collect(Collectors.toList());
    }

    /**
     * Get upcoming events
     */
    public List<EventDTO> getUpcomingEvents() {
        List<Event> events = eventRepository.findUpcomingEvents();
        return events.stream().map(this::mapEventToDTO).collect(Collectors.toList());
    }

    /**
     * Get event by ID
     */
    public EventDTO getEventById(Long id) {
        Optional<Event> event = eventRepository.findById(id);
        if (event.isEmpty()) {
            throw new BadRequestException("L'événement n'existe pas");
        }
        return mapEventToDTO(event.get());
    }

    /**
     * Update event
     */
    public EventDTO updateEvent(Long id, Long userId, EventDTO eventDTO) {
        Optional<Event> eventOpt = eventRepository.findById(id);
        if (eventOpt.isEmpty()) {
            throw new BadRequestException("L'événement n'existe pas");
        }

        Event event = eventOpt.get();
        if (!event.getCreateur().getId().equals(userId)) {
            throw new BadRequestException("Vous n'êtes pas autorisé à modifier cet événement");
        }

        if (eventDTO.getTitre() != null) {
            event.setTitre(eventDTO.getTitre());
        }
        if (eventDTO.getDescription() != null) {
            event.setDescription(eventDTO.getDescription());
        }
        if (eventDTO.getDateDebut() != null) {
            event.setDateDebut(eventDTO.getDateDebut());
        }
        if (eventDTO.getDateFin() != null) {
            event.setDateFin(eventDTO.getDateFin());
        }
        if (eventDTO.getLocalisation() != null) {
            event.setLocalisation(eventDTO.getLocalisation());
        }
        if (eventDTO.getImageUrl() != null) {
            event.setImageUrl(eventDTO.getImageUrl());
        }

        event.setDateModification(LocalDateTime.now());
        Event updatedEvent = eventRepository.save(event);
        return mapEventToDTO(updatedEvent);
    }

    /**
     * Delete event
     */
    public void deleteEvent(Long id, Long userId) {
        Optional<Event> eventOpt = eventRepository.findById(id);
        if (eventOpt.isEmpty()) {
            throw new BadRequestException("L'événement n'existe pas");
        }

        Event event = eventOpt.get();
        if (!event.getCreateur().getId().equals(userId)) {
            throw new BadRequestException("Vous n'êtes pas autorisé à supprimer cet événement");
        }

        eventRepository.delete(event);
    }

    /**
     * Search events
     */
    public List<EventDTO> searchEvents(String keyword) {
        List<Event> events = eventRepository.searchEvents(keyword);
        return events.stream().map(this::mapEventToDTO).collect(Collectors.toList());
    }

    /**
     * Get public events
     */
    public List<EventDTO> getPublicEvents() {
        List<Event> events = eventRepository.findPublicEvents();
        return events.stream().map(this::mapEventToDTO).collect(Collectors.toList());
    }

    /**
     * Map Event to EventDTO
     */
    private EventDTO mapEventToDTO(Event event) {
        EventDTO dto = new EventDTO();
        dto.setId(event.getId());
        dto.setTitre(event.getTitre());
        dto.setDescription(event.getDescription());
        dto.setDateDebut(event.getDateDebut());
        dto.setDateFin(event.getDateFin());
        dto.setLocalisation(event.getLocalisation());
        dto.setImageUrl(event.getImageUrl());
        dto.setCreateurId(event.getCreateur().getId());
        dto.setCreateurFirstName(event.getCreateur().getFirstName());
        dto.setCreateurLastName(event.getCreateur().getLastName());
        dto.setCreateurEmail(event.getCreateur().getEmail());
        dto.setNombreParticipants(event.getNombreParticipants());
        dto.setStatut(event.getStatut().toString());
        dto.setIsPublic(event.getIsPublic());
        dto.setDateCreation(event.getDateCreation());
        return dto;
    }
}
