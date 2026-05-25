package tn.entreprise.escproject.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:ESConnect}")
    private String appName;

    /**
     * Send an HTML email.
     *
     * @param to      recipient email address
     * @param subject email subject
     * @param html    HTML content
     */
    public void sendHtmlEmail(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email. Please try again later.");
        }
    }

    /**
     * Build and send a password reset email with professional HTML template.
     *
     * @param to             recipient email
     * @param firstName      user's first name
     * @param resetLink      the full reset URL
     * @param expirationMins token expiration in minutes
     */
    public void sendPasswordResetEmail(String to, String firstName, String resetLink, int expirationMins) {
        String subject = appName + " - Password Reset Request";
        String html = buildPasswordResetTemplate(firstName, resetLink, expirationMins);
        sendHtmlEmail(to, subject, html);
    }

    private String buildPasswordResetTemplate(String firstName, String resetLink, int expirationMins) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Password Reset</title>
            </head>
            <body style="margin: 0; padding: 0; background-color: #f8f9fc; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;">
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f8f9fc; padding: 40px 20px;">
                    <tr>
                        <td align="center">
                            <table role="presentation" width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.07); overflow: hidden;">
                                <!-- Header -->
                                <tr>
                                    <td style="background: linear-gradient(135deg, #1a1a2e 0%%, #d13535 100%%); padding: 40px 40px 30px;">
                                        <table role="presentation" width="100%%" cellpadding="0" cellspacing="0">
                                            <tr>
                                                <td>
                                                    <div style="width: 42px; height: 42px; background-color: #d13535; border-radius: 10px; display: inline-block; text-align: center; line-height: 42px;">
                                                        <span style="color: #ffffff; font-weight: 800; font-size: 1.3rem;">E</span>
                                                    </div>
                                                    <span style="color: #ffffff; font-size: 1.4rem; font-weight: 700; margin-left: 10px; vertical-align: middle;">%s</span>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                <!-- Body -->
                                <tr>
                                    <td style="padding: 40px;">
                                        <h1 style="color: #242d3b; font-size: 1.5rem; font-weight: 700; margin: 0 0 16px;">Password Reset Request</h1>
                                        <p style="color: #5f6b82; font-size: 0.95rem; line-height: 1.7; margin: 0 0 24px;">
                                            Hi <strong style="color: #242d3b;">%s</strong>,
                                        </p>
                                        <p style="color: #5f6b82; font-size: 0.95rem; line-height: 1.7; margin: 0 0 32px;">
                                            We received a request to reset your password. Click the button below to choose a new password. This link will expire in <strong>%d minutes</strong>.
                                        </p>
                                        <!-- CTA Button -->
                                        <table role="presentation" cellpadding="0" cellspacing="0" style="margin: 0 auto 32px;">
                                            <tr>
                                                <td style="background-color: #d13535; border-radius: 8px;">
                                                    <a href="%s" target="_blank" style="display: inline-block; padding: 14px 32px; color: #ffffff; font-size: 0.95rem; font-weight: 600; text-decoration: none;">
                                                        Reset My Password
                                                    </a>
                                                </td>
                                            </tr>
                                        </table>
                                        <!-- Fallback URL -->
                                        <p style="color: #8892a7; font-size: 0.8rem; line-height: 1.6; margin: 0 0 24px;">
                                            If the button doesn't work, copy and paste this link into your browser:<br>
                                            <a href="%s" style="color: #d13535; word-break: break-all;">%s</a>
                                        </p>
                                        <!-- Security Notice -->
                                        <div style="background-color: #fffbeb; border: 1px solid #fde68a; border-radius: 8px; padding: 16px; margin-top: 24px;">
                                            <p style="color: #92400e; font-size: 0.8rem; line-height: 1.6; margin: 0;">
                                                <strong>&#9888; Security Notice:</strong> If you did not request a password reset, please ignore this email. Your password will remain unchanged and your account is secure.
                                            </p>
                                        </div>
                                    </td>
                                </tr>
                                <!-- Footer -->
                                <tr>
                                    <td style="background-color: #f8f9fc; padding: 24px 40px; border-top: 1px solid #e4e8ef;">
                                        <p style="color: #8892a7; font-size: 0.75rem; line-height: 1.6; margin: 0; text-align: center;">
                                            This is an automated message from %s. Please do not reply to this email.<br>
                                            &copy; 2024 %s. All rights reserved.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(appName, firstName, expirationMins, resetLink, resetLink, resetLink, appName, appName);
    }
}
