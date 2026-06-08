package tn.entreprise.escproject.services;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tn.entreprise.escproject.dto.ForgotPasswordRequest;
import tn.entreprise.escproject.dto.ResetPasswordRequest;
import tn.entreprise.escproject.entite.PasswordResetToken;
import tn.entreprise.escproject.entite.TokenStatus;
import tn.entreprise.escproject.entite.User;
import tn.entreprise.escproject.exception.BadRequestException;
import tn.entreprise.escproject.repositories.PasswordResetTokenRepository;
import tn.entreprise.escproject.repositories.UserRepository;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.reset-token-expiration-minutes:30}")
    private int tokenExpirationMinutes;

    /**
     * Process forgot password request.
     * Always returns a generic success message to prevent email enumeration.
     */
    @Transactional
    public void processForgotPassword(ForgotPasswordRequest request) {
        log.info("Forgot password request received for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Forgot password failed: no account found for email {}", request.getEmail());
                    return new BadRequestException("No account found with this email address.");
                });

        String token = generateSecureToken();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setCreatedAt(LocalDateTime.now());
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(tokenExpirationMinutes));
        resetToken.setStatus(TokenStatus.ACTIVE);

        tokenRepository.save(resetToken);
        log.info("Password reset token created for user: {}", user.getEmail());

        String resetLink = frontendUrl + "/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(
                user.getEmail(),
                user.getFirstName(),
                resetLink,
                tokenExpirationMinutes
        );
    }

    /**
     * Validate token and reset the user's password.
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Password reset attempt with token");

        // Validate password confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        // Validate password strength (same policy as registration: 8+ chars, 2 digits, 2 special)
        validatePasswordStrength(request.getNewPassword());

        // Find and validate token
        PasswordResetToken resetToken = tokenRepository.findByTokenAndStatus(request.getToken(), TokenStatus.ACTIVE)
                .orElseThrow(() -> {
                    log.warn("Invalid or already used reset token");
                    return new BadRequestException("Invalid or expired password reset link. Please request a new one.");
                });

        // Check expiration
        if (resetToken.isExpired()) {
            resetToken.setStatus(TokenStatus.EXPIRED);
            tokenRepository.save(resetToken);
            log.warn("Expired reset token used for user: {}", resetToken.getUser().getEmail());
            throw new BadRequestException("Password reset link has expired. Please request a new one.");
        }

        // Update password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Mark token as used (prevent reuse)
        resetToken.setStatus(TokenStatus.USED);
        tokenRepository.save(resetToken);

        log.info("Password reset successfully for user: {}", user.getEmail());
    }

    /**
     * Validate that the token is valid (for frontend to check before showing the form).
     */
    public boolean validateToken(String token) {
        return tokenRepository.findByTokenAndStatus(token, TokenStatus.ACTIVE)
                .map(resetToken -> !resetToken.isExpired())
                .orElse(false);
    }

    /**
     * Generate a cryptographically secure random token.
     */
    private String generateSecureToken() {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    /**
     * Validate password strength: min 8 chars, at least 2 digits, at least 2 special characters.
     * Matches the frontend registration password policy.
     */
    private void validatePasswordStrength(String password) {
        if (password.length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters long");
        }

        long digitCount = password.chars().filter(Character::isDigit).count();
        if (digitCount < 2) {
            throw new BadRequestException("Password must contain at least 2 digits");
        }

        long specialCount = password.chars()
                .filter(ch -> !Character.isLetterOrDigit(ch))
                .count();
        if (specialCount < 2) {
            throw new BadRequestException("Password must contain at least 2 special characters");
        }
    }
}
