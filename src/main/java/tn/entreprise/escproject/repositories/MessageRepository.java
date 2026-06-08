package tn.entreprise.escproject.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import tn.entreprise.escproject.entite.Message;

import java.util.List;

public interface MessageRepository
        extends JpaRepository<Message, Long> {

    @Query("""
    SELECT m FROM Message m
    JOIN FETCH m.sender s
    LEFT JOIN FETCH m.receiver r
    LEFT JOIN FETCH m.group g
    WHERE m.id = :id
""")
    java.util.Optional<Message> findByIdWithRelations(Long id);

    //TOUS les messages entre deux utilisateurs. Dans les deux sens.
    @Query(
            value = """
    SELECT m FROM Message m
    JOIN FETCH m.sender s
    LEFT JOIN FETCH m.receiver r
    LEFT JOIN FETCH m.group g
    WHERE
    (m.sender.id = :user1
    AND m.receiver.id = :user2)

    OR

    (m.sender.id = :user2
    AND m.receiver.id = :user1)

    ORDER BY m.sentAt DESC /*les messages récents dabord.*/
""",
            countQuery = """
    SELECT COUNT(m) FROM Message m
    WHERE
    (m.sender.id = :user1
    AND m.receiver.id = :user2)

    OR

    (m.sender.id = :user2
    AND m.receiver.id = :user1)
"""
    )
    Page<Message> getConversation(
            Long user1,
            Long user2,
            Pageable pageable
    );

    @Query("""
        SELECT m FROM Message m
        JOIN FETCH m.sender s
        LEFT JOIN FETCH m.receiver r
        LEFT JOIN FETCH m.group g
        WHERE m.group.id = :groupId
        ORDER BY m.sentAt DESC
    """)
    List<Message> getGroupConversation(Long groupId);

    @Modifying
    @Transactional
    @Query("""
        DELETE FROM Message m
        WHERE m.group.id = :groupId
    """)
    void deleteByGroupId(Long groupId);
}