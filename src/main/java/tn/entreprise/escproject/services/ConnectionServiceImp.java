package tn.entreprise.escproject.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.entreprise.escproject.dto.ConnectionRequestDTO;
import tn.entreprise.escproject.entite.Connection;
import tn.entreprise.escproject.entite.User;
import tn.entreprise.escproject.entite.enums.ConnectionStatus;
import tn.entreprise.escproject.repositories.ConnectionRepository;
import tn.entreprise.escproject.repositories.UserRepository;
import tn.entreprise.escproject.services.Interfaces.IConnectionService;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConnectionServiceImp implements IConnectionService {

    private final ConnectionRepository connectionRepository;

    private final UserRepository userRepository;

    @Override
    public Connection sendConnectionRequest(
            ConnectionRequestDTO dto) {

        // ========================================
        // CHECK EXISTING CONNECTION
        // ========================================

        if (connectionRepository.existsConnectionBetween(

                dto.getSenderId(),

                dto.getReceiverId()
        )) {

            throw new RuntimeException(
                    "Connection already exists"
            );
        }

        // ========================================
        // GET USERS
        // ========================================

        User sender = userRepository.findById(
                        dto.getSenderId())

                .orElseThrow(() ->

                        new RuntimeException(
                                "Sender not found"
                        ));

        User receiver = userRepository.findById(
                        dto.getReceiverId())

                .orElseThrow(() ->

                        new RuntimeException(
                                "Receiver not found"
                        ));

        // ========================================
        // CREATE CONNECTION
        // ========================================

        Connection connection = Connection.builder()

                .sender(sender)

                .receiver(receiver)

                .status(ConnectionStatus.PENDING)

                .createdAt(LocalDateTime.now())

                .build();

        return connectionRepository.save(connection);
    }

    @Override
    public Connection acceptConnection(Long id) {

        Connection connection = connectionRepository.findByIdWithUsers(id)
                .orElseThrow(() ->
                        new RuntimeException("Connection not found"));

        connection.setStatus(ConnectionStatus.ACCEPTED);

        return connectionRepository.save(connection);
    }

    @Override
    public Connection rejectConnection(Long id) {

        Connection connection = connectionRepository.findByIdWithUsers(id)
                .orElseThrow(() ->
                        new RuntimeException("Connection not found"));

        connection.setStatus(ConnectionStatus.REJECTED);

        return connectionRepository.save(connection);
    }

    @Override
    public void deleteConnection(Long id) {

        connectionRepository.deleteById(id);
    }

    @Override
    public List<Connection> getUserConnections(Long userId) {

        return connectionRepository.findUserConnections(userId);
    }

    @Override
    public List<Connection> getPendingRequests(
            Long userId) {

        return connectionRepository
                .findPendingRequests(userId);
    }

    @Override
    public List<Connection> getSentRequests(
            Long userId) {

        return connectionRepository
                .findSentRequests(userId);
    }

    @Override
    public boolean connectionExists(

            Long senderId,

            Long receiverId) {

        return connectionRepository.existsConnectionBetween(

                senderId,

                receiverId
        );
    }
}
