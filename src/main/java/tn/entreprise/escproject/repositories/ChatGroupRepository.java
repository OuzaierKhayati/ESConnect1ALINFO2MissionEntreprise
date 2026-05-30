package tn.entreprise.escproject.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.entreprise.escproject.entite.ChatGroup;

import java.util.List;
import java.util.Optional;

public interface ChatGroupRepository extends JpaRepository<ChatGroup, Long> {

    @Query("""
        SELECT DISTINCT g FROM ChatGroup g
        JOIN g.members m
        WHERE m.id = :userId
        ORDER BY g.createdAt DESC
    """)
    List<ChatGroup> findGroupsForUser(@Param("userId") Long userId);

    @Query("""
        SELECT g FROM ChatGroup g
        LEFT JOIN FETCH g.members
        WHERE g.id = :groupId
    """)
    Optional<ChatGroup> findWithMembersById(@Param("groupId") Long groupId);
}