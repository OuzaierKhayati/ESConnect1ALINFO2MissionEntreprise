package tn.entreprise.escproject.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.entreprise.escproject.entite.Connection;
import java.util.Optional;

import java.util.List;

//verifier lannotation @Repository
public interface ConnectionRepository
        extends JpaRepository<Connection, Long> {

    @Query("""
        SELECT c FROM Connection c
        WHERE
        (c.sender.id = :userId
        OR c.receiver.id = :userId)
        AND c.status = 'ACCEPTED' 
    """)
    List<Connection> findUserConnections(
            @Param("userId") Long userId);

    @Query("""
    SELECT c FROM Connection c
    WHERE
    c.receiver.id = :userId
    AND c.status = 'PENDING'
""")
    List<Connection> findPendingRequests(
            @Param("userId") Long userId);

    @Query("""
    SELECT c FROM Connection c
    WHERE
    c.sender.id = :userId
    AND c.status = 'PENDING'
""")
    List<Connection> findSentRequests(
            @Param("userId") Long userId);

    @Query("""
    SELECT c FROM Connection c
    WHERE
    (c.sender.id = :senderId
    AND c.receiver.id = :receiverId)

    OR

    (c.sender.id = :receiverId
    AND c.receiver.id = :senderId)
""")
    Optional<Connection> findExistingConnection(
            @Param("senderId") Long senderId,
            @Param("receiverId") Long receiverId
    );

    @Query("""
    SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
    FROM Connection c
    WHERE
    (c.sender.id = :senderId
    AND c.receiver.id = :receiverId)

    OR

    (c.sender.id = :receiverId
    AND c.receiver.id = :senderId)
""")
    boolean existsConnectionBetween(
            @Param("senderId") Long senderId,
            @Param("receiverId") Long receiverId
    );
}
