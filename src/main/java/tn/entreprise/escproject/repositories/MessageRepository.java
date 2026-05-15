package tn.entreprise.escproject.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import tn.entreprise.escproject.entite.Message;

public interface MessageRepository
        extends JpaRepository<Message, Long> {
    //TOUS les messages entre deux utilisateurs. Dans les deux sens.
    @Query("""
    SELECT m FROM Message m
    WHERE
    (m.sender.id = :user1
    AND m.receiver.id = :user2)

    OR

    (m.sender.id = :user2
    AND m.receiver.id = :user1)

    ORDER BY m.sentAt DESC
""")
    Page<Message> getConversation(
            Long user1,
            Long user2,
            Pageable pageable
    );
}