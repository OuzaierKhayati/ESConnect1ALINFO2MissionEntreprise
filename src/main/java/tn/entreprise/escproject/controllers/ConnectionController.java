package tn.entreprise.escproject.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.entreprise.escproject.dto.ConnectionRequestDTO;
import tn.entreprise.escproject.dto.ConnectionResponseDTO;
import tn.entreprise.escproject.services.Interfaces.IConnectionService;

import java.util.List;

@RestController
@RequestMapping("/connections")
@RequiredArgsConstructor
public class ConnectionController {

    private final IConnectionService connectionService;

    @PostMapping
    public ConnectionResponseDTO sendConnectionRequest(
            @RequestBody ConnectionRequestDTO dto) {

        return connectionService.sendConnectionRequest(dto);
    }

    @PutMapping("/{id}/accept")
    public ConnectionResponseDTO acceptConnection(@PathVariable Long id) {

        return connectionService.acceptConnection(id);
    }

    @PutMapping("/{id}/reject")
    public ConnectionResponseDTO rejectConnection(@PathVariable Long id) {

        return connectionService.rejectConnection(id);
    }

    @DeleteMapping("/{id}")
    public void deleteConnection(@PathVariable Long id) {

        connectionService.deleteConnection(id);
    }

    @GetMapping("/user/{userId}")
    public List<ConnectionResponseDTO> getUserConnections(
            @PathVariable Long userId) {

        return connectionService.getUserConnections(userId);
    }

    @GetMapping("/pending/{userId}")
    public List<ConnectionResponseDTO> getPendingRequests(

            @PathVariable Long userId) {

        return connectionService
                .getPendingRequests(userId);
    }

    @GetMapping("/sent/{userId}")
    public List<ConnectionResponseDTO> getSentRequests(

            @PathVariable Long userId) {

        return connectionService
                .getSentRequests(userId);
    }

    @PostMapping("/check")
    public boolean checkConnection(

            @RequestBody ConnectionRequestDTO dto) {

        return connectionService
                .connectionExists(

                        dto.getSenderId(),

                        dto.getReceiverId()
                );
    }
}