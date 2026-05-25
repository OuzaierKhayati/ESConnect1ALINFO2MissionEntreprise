package tn.entreprise.escproject.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tn.entreprise.escproject.entite.AuthProvider;
import tn.entreprise.escproject.entite.RoleUser;
import tn.entreprise.escproject.entite.User;
import tn.entreprise.escproject.entite.UserStatus;
import tn.entreprise.escproject.repositories.UserRepository;
import tn.entreprise.escproject.utils.JwtUtil;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();

        String email = extractEmail(oAuth2User, registrationId, oauthToken);
        String name = extractName(oAuth2User, registrationId);
        String providerId = extractProviderId(oAuth2User, registrationId);

        if (email == null || email.isBlank()) {
            log.error("OAuth2 login failed: no email provided by {}", registrationId);
            String errorUrl = frontendUrl + "/login?error=" +
                    URLEncoder.encode("Email not provided by " + registrationId + ". Please use an account with a public email.", StandardCharsets.UTF_8);
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
            return;
        }

        AuthProvider provider = registrationId.equalsIgnoreCase("google") ? AuthProvider.GOOGLE : AuthProvider.GITHUB;

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            // Create new user
            user = new User();
            user.setEmail(email);
            user.setProvider(provider);
            user.setProviderId(providerId);
            user.setRoleUser(RoleUser.STUDENT);
            user.setUserStatus(UserStatus.ACTIVE);
            user.setOnline(true);

            // Split name into first/last
            String[] nameParts = name != null ? name.split(" ", 2) : new String[]{"User", ""};
            user.setFirstName(nameParts[0]);
            user.setLastName(nameParts.length > 1 ? nameParts[1] : "");

            userRepository.save(user);
            log.info("New OAuth2 user created: {} via {}", email, registrationId);
        } else {
            // Existing user — update provider info if they were LOCAL and now using OAuth
            if (user.getProvider() == AuthProvider.LOCAL) {
                // Account linking: allow OAuth login for existing LOCAL account
                user.setProvider(provider);
                user.setProviderId(providerId);
            }
            user.setOnline(true);
            userRepository.save(user);
            log.info("Existing user logged in via OAuth2: {} via {}", email, registrationId);
        }

        // Generate JWT
        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRoleUser().toString());

        // Redirect to frontend with token
        String redirectUrl = frontendUrl + "/oauth-callback?token=" + token
                + "&id=" + user.getId()
                + "&email=" + URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8)
                + "&firstName=" + URLEncoder.encode(user.getFirstName() != null ? user.getFirstName() : "", StandardCharsets.UTF_8)
                + "&lastName=" + URLEncoder.encode(user.getLastName() != null ? user.getLastName() : "", StandardCharsets.UTF_8)
                + "&role=" + user.getRoleUser().toString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private String extractEmail(OAuth2User oAuth2User, String registrationId, OAuth2AuthenticationToken oauthToken) {
        if ("google".equalsIgnoreCase(registrationId)) {
            return oAuth2User.getAttribute("email");
        } else if ("github".equalsIgnoreCase(registrationId)) {
            String email = oAuth2User.getAttribute("email");
            if (email == null || email.isBlank()) {
                // GitHub email is private — fetch from /user/emails API
                email = fetchGitHubEmail(oauthToken);
            }
            return email;
        }
        return null;
    }

    /**
     * Fetches the primary verified email from GitHub's /user/emails endpoint.
     * This is needed when the user has their email set to private.
     */
    private String fetchGitHubEmail(OAuth2AuthenticationToken oauthToken) {
        try {
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getName()
            );

            if (client == null || client.getAccessToken() == null) {
                log.warn("No authorized client found for GitHub email fetch");
                return null;
            }

            String accessToken = client.getAccessToken().getTokenValue();

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<List<Map<String, Object>>> resp = restTemplate.exchange(
                    "https://api.github.com/user/emails",
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            List<Map<String, Object>> emails = resp.getBody();
            if (emails != null) {
                // Find primary verified email
                for (Map<String, Object> emailObj : emails) {
                    Boolean primary = (Boolean) emailObj.get("primary");
                    Boolean verified = (Boolean) emailObj.get("verified");
                    if (Boolean.TRUE.equals(primary) && Boolean.TRUE.equals(verified)) {
                        return (String) emailObj.get("email");
                    }
                }
                // Fallback: any verified email
                for (Map<String, Object> emailObj : emails) {
                    Boolean verified = (Boolean) emailObj.get("verified");
                    if (Boolean.TRUE.equals(verified)) {
                        return (String) emailObj.get("email");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch GitHub email via API", e);
        }
        return null;
    }

    private String extractName(OAuth2User oAuth2User, String registrationId) {
        if ("google".equalsIgnoreCase(registrationId)) {
            return oAuth2User.getAttribute("name");
        } else if ("github".equalsIgnoreCase(registrationId)) {
            String name = oAuth2User.getAttribute("name");
            if (name == null || name.isBlank()) {
                name = oAuth2User.getAttribute("login");
            }
            return name;
        }
        return "User";
    }

    private String extractProviderId(OAuth2User oAuth2User, String registrationId) {
        if ("google".equalsIgnoreCase(registrationId)) {
            return oAuth2User.getAttribute("sub");
        } else if ("github".equalsIgnoreCase(registrationId)) {
            Object id = oAuth2User.getAttribute("id");
            return id != null ? id.toString() : null;
        }
        return null;
    }
}
