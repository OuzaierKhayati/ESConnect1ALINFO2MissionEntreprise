package tn.entreprise.escproject.services.Interfaces;

import tn.entreprise.escproject.dto.ConnectionRequestDTO;
import tn.entreprise.escproject.dto.ConnectionResponseDTO;

import java.util.List;

public interface IConnectionService {

    ConnectionResponseDTO sendConnectionRequest(ConnectionRequestDTO dto);

    ConnectionResponseDTO acceptConnection(Long id);

    ConnectionResponseDTO rejectConnection(Long id);

    void deleteConnection(Long id);

    List<ConnectionResponseDTO> getUserConnections(Long userId);

    List<ConnectionResponseDTO> getPendingRequests(Long userId);

    List<ConnectionResponseDTO> getSentRequests(Long userId);

    boolean connectionExists(Long senderId, Long receiverId);
}