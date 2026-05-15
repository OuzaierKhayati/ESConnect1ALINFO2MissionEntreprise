package tn.entreprise.escproject.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.entreprise.escproject.dto.ConnectionRequestDTO;
import tn.entreprise.escproject.entite.Connection;
import tn.entreprise.escproject.services.Interfaces.IConnectionService;

import java.util.List;

@RestController
@RequestMapping("/connections")
@RequiredArgsConstructor
public class ConnectionController {

    private final IConnectionService connectionService;

    @PostMapping
    public Connection sendConnectionRequest(
            @RequestBody ConnectionRequestDTO dto) {

        return connectionService.sendConnectionRequest(dto);
    }

    @PutMapping("/{id}/accept")
    public Connection acceptConnection(@PathVariable Long id) {

        return connectionService.acceptConnection(id);
    }

    @PutMapping("/{id}/reject")
    public Connection rejectConnection(@PathVariable Long id) {

        return connectionService.rejectConnection(id);
    }

    @DeleteMapping("/{id}")
    public void deleteConnection(@PathVariable Long id) {

        connectionService.deleteConnection(id);
    }

    @GetMapping("/user/{userId}")
    public List<Connection> getUserConnections(
            @PathVariable Long userId) {

        return connectionService.getUserConnections(userId);
    }
}