package tn.entreprise.escproject.services.Interfaces;

import tn.entreprise.escproject.dto.ConnectionRequestDTO;
import tn.entreprise.escproject.entite.Connection;

import java.util.List;

public interface IConnectionService {

    //Connection sendConnectionRequest(Connection connection);
    Connection sendConnectionRequest(ConnectionRequestDTO dto);

    Connection acceptConnection(Long id);

    Connection rejectConnection(Long id);

    void deleteConnection(Long id);

    List<Connection> getUserConnections(Long userId);

    List<Connection> getPendingRequests(Long userId);

    List<Connection> getSentRequests(Long userId);

    boolean connectionExists(Long senderId, Long receiverId);
}