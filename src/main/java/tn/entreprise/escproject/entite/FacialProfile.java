package tn.entreprise.escproject.entite;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Stores facial recognition embeddings for a user.
 * Each user can have at most one FacialProfile.
 * Embeddings are stored as base64-encoded comma-separated float values.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "facial_profiles")
public class FacialProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * One-to-one relationship with User.
     * If user is deleted, facial profile is also deleted.
     */
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore
    private User user;

    /**
     * Averaged facial embedding (128-dimensional vector).
     * Stored as base64-encoded string of comma-separated floats.
     * Example: "0.1,0.2,0.3,...,0.128"
     */
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String avgEmbedding;

    /**
     * Number of samples used to compute the average embedding.
     * Helps track quality of enrollment.
     */
    @Column(nullable = false)
    private Integer sampleCount = 0;

    /**
     * Timestamp when facial profile was created/enrolled.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime enrolledAt;

    /**
     * Timestamp when facial profile was last updated.
     * Could be used to trigger re-enrollment.
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Whether this facial profile is currently active.
     * Users can disable facial recognition without deleting the profile.
     */
    @Column(nullable = false)
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        enrolledAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
