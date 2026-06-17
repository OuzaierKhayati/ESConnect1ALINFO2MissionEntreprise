package tn.entreprise.escproject.services;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import tn.entreprise.escproject.dto.FacialAuthRequest;
import tn.entreprise.escproject.dto.FacialEnrollmentRequest;
import tn.entreprise.escproject.entite.FacialProfile;
import tn.entreprise.escproject.entite.User;
import tn.entreprise.escproject.exception.BadRequestException;
import tn.entreprise.escproject.exception.ResourceNotFoundException;
import tn.entreprise.escproject.repositories.FacialProfileRepository;

/**
 * Service for facial recognition operations.
 * Handles enrollment, verification, and embedding comparison.
 */
@Service
public class FacialRecognitionService {

    private static final Logger log = LoggerFactory.getLogger(FacialRecognitionService.class);

    /**
     * Euclidean distance threshold for facial match.
     * Distances below this threshold indicate a match.
     * Value between 0.4-0.6 is standard; using 0.55 for balance.
     */
    private static final double DISTANCE_THRESHOLD = 0.62;

    /**
     * Minimum number of embedding samples for enrollment.
     */
    private static final int MIN_SAMPLES = 3;

    /**
     * Maximum number of embedding samples per enrollment.
     */
    private static final int MAX_SAMPLES = 5;

    @Autowired
    private FacialProfileRepository facialProfileRepository;

    @Autowired
    private EntityManager entityManager;

    /**
     * Enroll a user with facial embeddings.
     * Averages multiple embeddings to create a robust reference profile.
     *
     * @param user The user to enroll
     * @param request Enrollment request with embeddings
     * @return Created or updated FacialProfile
     */
    public FacialProfile enrollUser(User user, FacialEnrollmentRequest request) {
        log.info("Enrolling facial profile for user: {}", user.getEmail());

        if (request.getEmbeddings() == null || request.getEmbeddings().length == 0) {
            throw new BadRequestException("At least one embedding is required for enrollment");
        }

        if (request.getEmbeddings().length < MIN_SAMPLES) {
            throw new BadRequestException(
                String.format("Minimum %d facial samples required, got %d",
                    MIN_SAMPLES, request.getEmbeddings().length)
            );
        }

        if (request.getEmbeddings().length > MAX_SAMPLES) {
            throw new BadRequestException(
                String.format("Maximum %d facial samples allowed, got %d",
                    MAX_SAMPLES, request.getEmbeddings().length)
            );
        }

        try {
            // Convert embeddings to float arrays
            float[][] embeddings = new float[request.getEmbeddings().length][];
            for (int i = 0; i < request.getEmbeddings().length; i++) {
                embeddings[i] = decodeEmbedding(request.getEmbeddings()[i]);
            }

            // Compute average embedding
            float[] avgEmbedding = averageEmbeddings(embeddings);

            // Check if user already has a facial profile
            FacialProfile facialProfile = facialProfileRepository.findByUser(user)
                .orElse(new FacialProfile());

            facialProfile.setUser(user);
            facialProfile.setAvgEmbedding(encodeEmbedding(avgEmbedding));
            facialProfile.setSampleCount(request.getEmbeddings().length);
            facialProfile.setIsActive(true);

            FacialProfile saved = facialProfileRepository.save(facialProfile);
            log.info("Facial profile enrolled successfully for user: {}", user.getEmail());
            return saved;

        } catch (Exception e) {
            log.error("Error enrolling facial profile for user: {}", user.getEmail(), e);
            throw new BadRequestException("Failed to process facial embeddings: " + e.getMessage());
        }
    }

    /**
     * Verify a user's facial embedding against their stored profile.
     * Returns true if the distance is below the threshold.
     *
     * @param user The user to verify
     * @param request Authentication request with embedding
     * @return true if verification successful, false otherwise
     */
    public boolean verifyFace(User user, FacialAuthRequest request) {
        log.info("Verifying facial embedding for user: {}", user.getEmail());

        if (request.getEmbedding() == null || request.getEmbedding().isEmpty()) {
            throw new BadRequestException("Embedding is required for verification");
        }

        FacialProfile facialProfile = facialProfileRepository.findByUser(user)
            .orElseThrow(() -> new ResourceNotFoundException("Facial profile not found for user"));

        if (!facialProfile.getIsActive()) {
            throw new BadRequestException("Facial profile is not active");
        }

        try {
            // Decode both embeddings
            float[] capturedEmbedding = decodeEmbedding(request.getEmbedding());
            float[] storedEmbedding = decodeEmbedding(facialProfile.getAvgEmbedding());

            // Calculate Euclidean distance
            double distance = euclideanDistance(capturedEmbedding, storedEmbedding);

            log.debug("Facial verification distance: {} (threshold: {})", distance, DISTANCE_THRESHOLD);

            boolean isMatch = distance < DISTANCE_THRESHOLD;
            if (isMatch) {
                log.info("Facial verification successful for user: {}", user.getEmail());
            } else {
                log.warn("Facial verification failed for user: {} (distance: {})", user.getEmail(), distance);
            }

            return isMatch;

        } catch (Exception e) {
            log.error("Error verifying facial embedding for user: {}", user.getEmail(), e);
            throw new BadRequestException("Failed to verify facial embedding: " + e.getMessage());
        }
    }

    /**
     * Delete facial profile for a user.
     * Called when user chooses to disable facial authentication.
     *
     * @param user The user whose facial profile to delete
     */
    @Transactional
    public void deleteFacialProfile(User user) {
        log.info("Deleting facial profile for user: {}", user.getEmail());

        // Verify profile exists before deletion
        boolean hasProfile = facialProfileRepository.findByUser(user).isPresent();
        if (!hasProfile) {
            throw new ResourceNotFoundException("No facial profile found to delete");
        }

        // Use direct JPQL delete query to bypass entity caching issues
        int deletedCount = facialProfileRepository.deleteByUserId(user.getId());
        
        if (deletedCount == 0) {
            log.error("Direct delete query returned 0 rows for user: {}", user.getEmail());
            throw new BadRequestException("Failed to remove facial profile. Please try again.");
        }

        log.info("Facial profile successfully deleted for user: {}", user.getEmail());
    }

    /**
     * Check if a user has an active facial profile.
     *
     * @param userId The user ID
     * @return true if user has active facial profile
     */
    public boolean hasActiveFacialProfile(Long userId) {
        return facialProfileRepository.existsByUser_IdAndIsActiveTrue(userId);
    }

    /**
     * Re-activate a previously disabled facial profile.
     *
     * @param user The user whose profile to reactivate
     */
    public void reactivateFacialProfile(User user) {
        log.info("Reactivating facial profile for user: {}", user.getEmail());
        FacialProfile facialProfile = facialProfileRepository.findByUser(user)
            .orElseThrow(() -> new ResourceNotFoundException("Facial profile not found"));
        facialProfile.setIsActive(true);
        facialProfile.setUpdatedAt(LocalDateTime.now());
        facialProfileRepository.save(facialProfile);
    }

    // ==================== Helper Methods ====================

    /**
     * Calculate Euclidean distance between two embeddings.
     * Lower distance = more similar faces.
     *
     * @param embedding1 First embedding vector
     * @param embedding2 Second embedding vector
     * @return Euclidean distance
     */
    private double euclideanDistance(float[] embedding1, float[] embedding2) {
        if (embedding1.length != embedding2.length) {
            throw new IllegalArgumentException("Embeddings must have same dimensions");
        }

        double sum = 0.0;
        for (int i = 0; i < embedding1.length; i++) {
            double diff = embedding1[i] - embedding2[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    /**
     * Compute average of multiple embeddings.
     * Used during enrollment to create a robust reference profile.
     *
     * @param embeddings Array of embedding vectors
     * @return Averaged embedding
     */
    private float[] averageEmbeddings(float[][] embeddings) {
        if (embeddings.length == 0) {
            throw new IllegalArgumentException("No embeddings provided");
        }

        int embeddingSize = embeddings[0].length;
        float[] average = new float[embeddingSize];

        for (float[] embedding : embeddings) {
            if (embedding.length != embeddingSize) {
                throw new IllegalArgumentException("All embeddings must have same dimension");
            }
            for (int i = 0; i < embeddingSize; i++) {
                average[i] += embedding[i];
            }
        }

        for (int i = 0; i < embeddingSize; i++) {
            average[i] /= embeddings.length;
        }

        return average;
    }

    /**
     * Encode float array as comma-separated base64 string.
     * Used for storing embeddings in database.
     *
     * @param embedding Float array to encode
     * @return Encoded string
     */
    private String encodeEmbedding(float[] embedding) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        return sb.toString();
    }

    /**
     * Decode comma-separated string back to float array.
     * Used for retrieving embeddings from database.
     *
     * @param encoded Encoded string
     * @return Float array
     */
    private float[] decodeEmbedding(String encoded) {
        String[] parts = encoded.split(",");
        float[] embedding = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            embedding[i] = Float.parseFloat(parts[i].trim());
        }
        return embedding;
    }
}
