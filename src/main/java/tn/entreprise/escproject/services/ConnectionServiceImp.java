package tn.entreprise.escproject.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import tn.entreprise.escproject.dto.ConnectionRequestDTO;
import tn.entreprise.escproject.dto.ConnectionResponseDTO;
import tn.entreprise.escproject.dto.ConnectionUserDTO;
import tn.entreprise.escproject.entite.Connection;
import tn.entreprise.escproject.entite.User;
import tn.entreprise.escproject.entite.enums.ConnectionStatus;
import tn.entreprise.escproject.repositories.ConnectionRepository;
import tn.entreprise.escproject.repositories.UserProfileRepository;
import tn.entreprise.escproject.repositories.UserRepository;
import tn.entreprise.escproject.services.Interfaces.IConnectionService;

@Service
@RequiredArgsConstructor
public class ConnectionServiceImp implements IConnectionService {

    private final ConnectionRepository connectionRepository;

    private final UserRepository userRepository;

    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional
    public ConnectionResponseDTO sendConnectionRequest(
            ConnectionRequestDTO dto) {

        if (connectionRepository.existsConnectionBetween(
                dto.getSenderId(),
                dto.getReceiverId())) {
            throw new RuntimeException("Connection already exists");
        }

        User sender = userRepository.findById(dto.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        User receiver = userRepository.findById(dto.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Connection connection = Connection.builder()
                .sender(sender)
                .receiver(receiver)
                .status(ConnectionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        return toDto(connectionRepository.save(connection));
    }

    @Override
    @Transactional
    public ConnectionResponseDTO acceptConnection(Long id) {
        Connection connection = connectionRepository.findByIdWithUsers(id)
                .orElseThrow(() -> new RuntimeException("Connection not found"));

        connection.setStatus(ConnectionStatus.ACCEPTED);

        return toDto(connectionRepository.save(connection));
    }

    @Override
    @Transactional
    public ConnectionResponseDTO rejectConnection(Long id) {
        Connection connection = connectionRepository.findByIdWithUsers(id)
                .orElseThrow(() -> new RuntimeException("Connection not found"));

        connection.setStatus(ConnectionStatus.REJECTED);

        return toDto(connectionRepository.save(connection));
    }

    @Override
    public void deleteConnection(Long id) {
        connectionRepository.deleteById(id);
    }

    @Override
    public List<ConnectionResponseDTO> getUserConnections(Long userId) {
        return connectionRepository.findUserConnections(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<ConnectionResponseDTO> getPendingRequests(Long userId) {
        return connectionRepository.findPendingRequests(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<ConnectionResponseDTO> getSentRequests(Long userId) {
        return connectionRepository.findSentRequests(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public boolean connectionExists(Long senderId, Long receiverId) {
        return connectionRepository.existsConnectionBetween(senderId, receiverId);
    }

    private ConnectionResponseDTO toDto(Connection connection) {
        return ConnectionResponseDTO.builder()
                .id(connection.getId())
                .sender(toUserDto(connection.getSender()))
                .receiver(toUserDto(connection.getReceiver()))
                .status(connection.getStatus())
                .createdAt(connection.getCreatedAt())
                .build();
    }

    private ConnectionUserDTO toUserDto(User user) {
        if (user == null) {
            return null;
        }

        return ConnectionUserDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .roleUser(user.getRoleUser().name())
                .profilePictureUrl(userProfileRepository.findByUserId(user.getId())
                    .map(profile -> profile.getProfilePictureUrl())
                    .orElse(null))
                .build();
    }
}
