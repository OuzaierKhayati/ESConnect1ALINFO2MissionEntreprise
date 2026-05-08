package tn.entreprise.escproject.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tn.entreprise.escproject.entite.Connection;

import java.util.List;

public interface ConnectionRepository
        extends JpaRepository<Connection, Long> {

    @Query("""
        SELECT c FROM Connection c
        WHERE
        (c.sender.id = :userId
        OR c.receiver.id = :userId)
        AND c.status = 'ACCEPTED'
    """)
    List<Connection> findUserConnections(Long userId);
}