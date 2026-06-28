package tn.entreprise.escproject.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.entreprise.escproject.dto.*;
import tn.entreprise.escproject.entite.*;
import tn.entreprise.escproject.exception.BadRequestException;
import tn.entreprise.escproject.exception.ConflictException;
import tn.entreprise.escproject.exception.ResourceNotFoundException;
import tn.entreprise.escproject.exception.UnauthorizedException;
import tn.entreprise.escproject.repositories.EventRegistrationRepository;
import tn.entreprise.escproject.repositories.EventRepository;
import tn.entreprise.escproject.repositories.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventServiceImp {

    private static final Logger log = LoggerFactory.getLogger(EventServiceImp.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventRegistrationRepository registrationRepository;

    @Autowired
    private UserRepository userRepository;

    // ======== Event CRUD ========

    @Transactional
    public EventResponse createEvent(EventRequest request, Long organizerId) {
        validateEventRequest(request);
        User organizer = findUserOrThrow(organizerId);

        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setEventType(parseEventType(request.getEventType()));
        event.setStartDate(parseDate(request.getStartDate()));
        event.setEndDate(request.getEndDate() != null ? parseDate(request.getEndDate()) : null);
        event.setLocation(request.getLocation());
        event.setOnline(request.isOnline());
        event.setMeetingLink(request.getMeetingLink());
        event.setCapacity(request.getCapacity());
        event.setCoverImageUrl(request.getCoverImageUrl());
        event.setPublished(false);
        event.setCreatedAt(LocalDateTime.now());
        event.setOrganizer(organizer);

        eventRepository.save(event);
        log.info("Event '{}' created by user {}", event.getTitle(), organizerId);
        return toResponse(event, organizerId);
    }

    @Transactional
    public EventResponse updateEvent(Long eventId, EventRequest request, Long organizerId) {
        Event event = findEventOrThrow(eventId);
        if (!event.getOrganizer().getId().equals(organizerId)) {
            throw new UnauthorizedException("You can only edit your own events");
        }

        if (request.getTitle() != null) event.setTitle(request.getTitle());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getEventType() != null) event.setEventType(parseEventType(request.getEventType()));
        if (request.getStartDate() != null) event.setStartDate(parseDate(request.getStartDate()));
        if (request.getEndDate() != null) event.setEndDate(parseDate(request.getEndDate()));
        if (request.getLocation() != null) event.setLocation(request.getLocation());
        event.setOnline(request.isOnline());
        if (request.getMeetingLink() != null) event.setMeetingLink(request.getMeetingLink());
        if (request.getCapacity() > 0) event.setCapacity(request.getCapacity());
        if (request.getCoverImageUrl() != null) event.setCoverImageUrl(request.getCoverImageUrl());

        eventRepository.save(event);
        return toResponse(event, organizerId);
    }

    @Transactional
    public void deleteEvent(Long eventId, Long organizerId) {
        Event event = findEventOrThrow(eventId);
        if (!event.getOrganizer().getId().equals(organizerId)) {
            throw new UnauthorizedException("You can only delete your own events");
        }
        eventRepository.delete(event);
        log.info("Event {} deleted by user {}", eventId, organizerId);
    }

    @Transactional
    public EventResponse togglePublish(Long eventId, Long organizerId) {
        Event event = findEventOrThrow(eventId);
        if (!event.getOrganizer().getId().equals(organizerId)) {
            throw new UnauthorizedException("You can only manage your own events");
        }
        event.setPublished(!event.isPublished());
        eventRepository.save(event);
        return toResponse(event, organizerId);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getAllPublishedEvents(Long currentUserId) {
        return eventRepository.findByPublishedTrueOrderByStartDateAsc().stream()
                .map(e -> toResponse(e, currentUserId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getMyEvents(Long organizerId) {
        User organizer = findUserOrThrow(organizerId);
        return eventRepository.findByOrganizerOrderByCreatedAtDesc(organizer).stream()
                .map(e -> toResponse(e, organizerId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventResponse getEventById(Long eventId, Long currentUserId) {
        return toResponse(findEventOrThrow(eventId), currentUserId);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> searchEvents(String query, Long currentUserId) {
        return eventRepository.findByPublishedTrueAndTitleContainingIgnoreCaseOrderByStartDateAsc(query).stream()
                .map(e -> toResponse(e, currentUserId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getEventsByType(String type, Long currentUserId) {
        EventType eventType = parseEventType(type);
        return eventRepository.findByPublishedTrueAndEventTypeOrderByStartDateAsc(eventType).stream()
                .map(e -> toResponse(e, currentUserId))
                .collect(Collectors.toList());
    }

    // ======== Registrations ========

    @Transactional
    public EventRegistrationResponse register(Long eventId, Long userId) {
        Event event = findEventOrThrow(eventId);
        User user = findUserOrThrow(userId);

        if (!event.isPublished()) {
            throw new BadRequestException("This event is not open for registration");
        }
        if (registrationRepository.existsByEventAndAttendee(event, user)) {
            throw new ConflictException("You are already registered for this event");
        }
        long count = registrationRepository.countByEvent(event);
        if (event.getCapacity() > 0 && count >= event.getCapacity()) {
            throw new BadRequestException("This event is at full capacity");
        }

        EventRegistration reg = new EventRegistration();
        reg.setEvent(event);
        reg.setAttendee(user);
        reg.setRegisteredAt(LocalDateTime.now());
        registrationRepository.save(reg);

        log.info("User {} registered for event {}", userId, eventId);
        return toRegistrationResponse(reg);
    }

    @Transactional
    public void cancelRegistration(Long eventId, Long userId) {
        Event event = findEventOrThrow(eventId);
        User user = findUserOrThrow(userId);
        EventRegistration reg = registrationRepository.findByEventAndAttendee(event, user)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found"));
        registrationRepository.delete(reg);
        log.info("User {} cancelled registration for event {}", userId, eventId);
    }

    @Transactional(readOnly = true)
    public List<EventRegistrationResponse> getRegistrations(Long eventId, Long requesterId) {
        Event event = findEventOrThrow(eventId);
        if (!event.getOrganizer().getId().equals(requesterId)) {
            throw new UnauthorizedException("Only the organizer can view registrations");
        }
        return registrationRepository.findByEvent(event).stream()
                .map(this::toRegistrationResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getMyRegisteredEvents(Long userId) {
        User user = findUserOrThrow(userId);
        return registrationRepository.findByAttendee(user).stream()
                .map(reg -> toResponse(reg.getEvent(), userId))
                .collect(Collectors.toList());
    }

    // ======== Helpers ========

    private Event findEventOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + id));
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private EventType parseEventType(String type) {
        try {
            return EventType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid event type: " + type);
        }
    }

    private LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return LocalDateTime.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e1) {
            try {
                return LocalDateTime.parse(dateStr + " 00:00:00", DATE_FORMATTER);
            } catch (DateTimeParseException e2) {
                throw new BadRequestException("Invalid date format: " + dateStr + ". Use yyyy-MM-dd HH:mm:ss");
            }
        }
    }

    private void validateEventRequest(EventRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new BadRequestException("Event title is required");
        }
        if (request.getStartDate() == null || request.getStartDate().isBlank()) {
            throw new BadRequestException("Event start date is required");
        }
        if (request.getEventType() == null || request.getEventType().isBlank()) {
            throw new BadRequestException("Event type is required");
        }
    }

    private EventResponse toResponse(Event event, Long currentUserId) {
        long regCount = registrationRepository.countByEvent(event);
        boolean registered = false;
        if (currentUserId != null) {
            User currentUser = userRepository.findById(currentUserId).orElse(null);
            if (currentUser != null) {
                registered = registrationRepository.existsByEventAndAttendee(event, currentUser);
            }
        }

        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventType(event.getEventType() != null ? event.getEventType().toString() : null)
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .location(event.getLocation())
                .online(event.isOnline())
                .meetingLink(event.getMeetingLink())
                .capacity(event.getCapacity())
                .coverImageUrl(event.getCoverImageUrl())
                .published(event.isPublished())
                .createdAt(event.getCreatedAt())
                .organizerId(event.getOrganizer() != null ? event.getOrganizer().getId() : null)
                .organizerName(event.getOrganizer() != null ? event.getOrganizer().getFirstName() + " " + event.getOrganizer().getLastName() : null)
                .organizerProfilePictureUrl(event.getOrganizer() != null && event.getOrganizer().getProfile() != null ? event.getOrganizer().getProfile().getProfilePictureUrl() : null)
                .registrationCount(regCount)
                .registeredByCurrentUser(registered)
                .organizedByCurrentUser(currentUserId != null && event.getOrganizer() != null && event.getOrganizer().getId().equals(currentUserId))
                .build();
    }

    private EventRegistrationResponse toRegistrationResponse(EventRegistration reg) {
        return EventRegistrationResponse.builder()
                .id(reg.getId())
                .eventId(reg.getEvent().getId())
                .eventTitle(reg.getEvent().getTitle())
                .userId(reg.getAttendee().getId())
                .userName(reg.getAttendee().getFirstName() + " " + reg.getAttendee().getLastName())
                .userProfilePictureUrl(reg.getAttendee().getProfile() != null ? reg.getAttendee().getProfile().getProfilePictureUrl() : null)
                .registeredAt(reg.getRegisteredAt())
                .build();
    }
}
