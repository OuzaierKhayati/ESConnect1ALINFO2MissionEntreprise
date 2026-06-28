package tn.entreprise.escproject.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tn.entreprise.escproject.dto.*;
import tn.entreprise.escproject.entite.User;
import tn.entreprise.escproject.exception.ResourceNotFoundException;
import tn.entreprise.escproject.repositories.UserRepository;
import tn.entreprise.escproject.services.EventServiceImp;

import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {

    @Autowired
    private EventServiceImp eventService;

    @Autowired
    private UserRepository userRepository;

    // ===== CRUD =====

    @PostMapping
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @RequestBody EventRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        EventResponse response = eventService.createEvent(request, user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Event created successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @PathVariable Long id,
            @RequestBody EventRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        EventResponse response = eventService.updateEvent(id, request, user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Event updated", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        eventService.deleteEvent(id, user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Event deleted", null));
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<EventResponse>> togglePublish(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        EventResponse response = eventService.togglePublish(id, user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Event publish status updated", response));
    }

    // ===== Queries =====

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponse>>> getAllEvents(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        List<EventResponse> events = eventService.getAllPublishedEvents(user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Events retrieved", events));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getMyEvents(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        List<EventResponse> events = eventService.getMyEvents(user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Your events retrieved", events));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEventById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        EventResponse event = eventService.getEventById(id, user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Event retrieved", event));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<EventResponse>>> searchEvents(
            @RequestParam String query,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        List<EventResponse> events = eventService.searchEvents(query, user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Search results", events));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getEventsByType(
            @PathVariable String type,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        List<EventResponse> events = eventService.getEventsByType(type, user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Events by type", events));
    }

    // ===== Registrations =====

    @PostMapping("/{id}/register")
    public ResponseEntity<ApiResponse<EventRegistrationResponse>> register(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        EventRegistrationResponse response = eventService.register(id, user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Registered successfully", response));
    }

    @DeleteMapping("/{id}/register")
    public ResponseEntity<ApiResponse<Void>> cancelRegistration(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        eventService.cancelRegistration(id, user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Registration cancelled", null));
    }

    @GetMapping("/{id}/registrations")
    public ResponseEntity<ApiResponse<List<EventRegistrationResponse>>> getRegistrations(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        List<EventRegistrationResponse> registrations = eventService.getRegistrations(id, user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Registrations retrieved", registrations));
    }

    @GetMapping("/registered")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getMyRegisteredEvents(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        List<EventResponse> events = eventService.getMyRegisteredEvents(user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Registered events retrieved", events));
    }

    // ===== Helper =====

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
