package tn.entreprise.escproject.repositories;

import tn.entreprise.escproject.entite.FacialProfile;
import tn.entreprise.escproject.entite.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Repository for FacialProfile entity.
 * Handles all database operations for facial recognition profiles.
 */
@Repository
public interface FacialProfileRepository extends JpaRepository<FacialProfile, Long> {

    /**
     * Find facial profile by user.
     * @param user The user entity
     * @return Optional containing FacialProfile if found
     */
    Optional<FacialProfile> findByUser(User user);

    /**
     * Find facial profile by user ID.
     * @param userId The user ID
     * @return Optional containing FacialProfile if found
     */
    Optional<FacialProfile> findByUser_Id(Long userId);

    /**
     * Check if a user has an active facial profile.
     * @param userId The user ID
     * @return true if user has an active facial profile
     */
    boolean existsByUser_IdAndIsActiveTrue(Long userId);

    /**
     * Delete facial profile by user ID.
     * @param userId The user ID
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    long deleteByUser_Id(Long userId);

    /**
     * Delete facial profile directly via JPQL for guaranteed database deletion.
     * Uses direct database delete bypassing entity lifecycle.
     * @param userId The user ID
     * @return Number of rows deleted
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM FacialProfile fp WHERE fp.user.id = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}
