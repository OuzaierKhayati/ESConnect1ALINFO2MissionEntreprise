package tn.entreprise.escproject.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import tn.entreprise.escproject.dto.ApiResponse;
import tn.entreprise.escproject.dto.CurrentPasswordRequest;
import tn.entreprise.escproject.dto.ForgotPasswordRequest;
import tn.entreprise.escproject.dto.ResetPasswordRequest;
import tn.entreprise.escproject.services.PasswordResetService;

@RestController
@RequestMapping("/password")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    /**
     * Request a password reset email.
     * Returns generic success message regardless of whether email exists (security best practice).
     */
    @PostMapping("/forgot")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.processForgotPassword(request);
        return ResponseEntity.ok(
                ApiResponse.success("A password reset link has been sent to your email address.")
        );
    }

    /**
     * Reset password using a valid token.
     */
    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(
                ApiResponse.success("Your password has been reset successfully. You can now log in with your new password.")
        );
    }

        /**
         * Authenticated flow used from profile settings.
         * Validates current password then triggers existing forgot-password workflow.
         */
        @PostMapping("/change-request")
        public ResponseEntity<ApiResponse<Void>> requestPasswordChange(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CurrentPasswordRequest request) {
        passwordResetService.processAuthenticatedPasswordChangeRequest(
            userDetails.getUsername(),
            request.getCurrentPassword());
        return ResponseEntity.ok(
            ApiResponse.success("Password reset instructions have been sent to your email.")
        );
        }

    /**
     * Validate a reset token (used by frontend to verify before showing the reset form).
     */
    @GetMapping("/validate-token")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@RequestParam String token) {
        boolean valid = passwordResetService.validateToken(token);
        if (valid) {
            return ResponseEntity.ok(ApiResponse.success("Token is valid", true));
        }
        return ResponseEntity.ok(ApiResponse.error("Invalid or expired token", false));
    }
}
