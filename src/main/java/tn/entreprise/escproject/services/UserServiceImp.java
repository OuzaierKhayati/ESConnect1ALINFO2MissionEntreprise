package tn.entreprise.escproject.services;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import tn.entreprise.escproject.dto.LoginRequest;
import tn.entreprise.escproject.dto.LoginResponse;
import tn.entreprise.escproject.dto.RegisterRequest;
import tn.entreprise.escproject.dto.UserResponse;
import tn.entreprise.escproject.dto.UserSearchResult;
import tn.entreprise.escproject.entite.RoleUser;
import tn.entreprise.escproject.entite.User;
import tn.entreprise.escproject.entite.UserProfile;
import tn.entreprise.escproject.entite.UserStatus;
import tn.entreprise.escproject.exception.BadRequestException;
import tn.entreprise.escproject.exception.ConflictException;
import tn.entreprise.escproject.exception.ResourceNotFoundException;
import tn.entreprise.escproject.exception.UnauthorizedException;
import tn.entreprise.escproject.repositories.UserProfileRepository;
import tn.entreprise.escproject.repositories.UserRepository;
import tn.entreprise.escproject.services.Interfaces.IService;
import tn.entreprise.escproject.services.Interfaces.IUserService;
import tn.entreprise.escproject.utils.JwtUtil;

@Service
public class UserServiceImp implements IService<User>, IUserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImp.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public User add(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        log.info("Adding new user with email: {}", user.getEmail());
        return userRepository.save(user);
    }

    @Override
    public User update(User user) {
        log.info("Updating user with id: {}", user.getId());
        return userRepository.save(user);
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting user with id: {}", id);
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public List<User> getAll() {
        return (List<User>) userRepository.findAll();
    }

    @Override
    public List<User> addAll(List<User> users) {
        users.forEach(user -> user.setPassword(passwordEncoder.encode(user.getPassword())));
        return (List<User>) userRepository.saveAll(users);
    }

    @Override
    public UserResponse registerUser(RegisterRequest registerRequest) {
        log.info("Registration attempt for email: {}", registerRequest.getEmail());

        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            log.warn("Registration failed: email already exists - {}", registerRequest.getEmail());
            throw new ConflictException("An account with this email already exists");
        }

        if (Objects.equals(registerRequest.getRoleUser(), RoleUser.ADMIN.toString())) {
            log.warn("Registration failed: attempted ADMIN role assignment for {}", registerRequest.getEmail());
            throw new BadRequestException("Cannot create account with ADMIN role");
        }

        RoleUser roleUser;
        try {
            roleUser = RoleUser.valueOf(registerRequest.getRoleUser().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role specified: " + registerRequest.getRoleUser());
        }

        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(Objects.requireNonNull(encodedPassword));
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setDateOfBirth(registerRequest.getDateOfBirth());
        user.setRoleUser(roleUser);
        user.setUserStatus(getInitialStatusForRole(roleUser));

        userRepository.save(user);
        log.info("User registered successfully: {} with role: {}", user.getEmail(), roleUser);
        return convertToUserResponse(user);
    }

    private UserStatus getInitialStatusForRole(RoleUser roleUser) {
        return switch (roleUser) {
            // External users and recruiters can access directly after signup.
            case FORMATEUR, RECRUITER -> UserStatus.ACTIVE;
            // University-internal roles require admin approval.
            case STUDENT, PROFESSOR -> UserStatus.PENDING;
            // ADMIN creation is blocked above; keep a safe fallback.
            default -> UserStatus.PENDING;
        };
    }

    @Override
    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        log.info("Login attempt for email: {}", loginRequest.getEmail());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            log.warn("Login failed: invalid credentials for {}", loginRequest.getEmail());
            throw new UnauthorizedException("Invalid email or password");
        } catch (Exception ex) {
            log.error("Login failed: unexpected auth error for {}", loginRequest.getEmail(), ex);
            throw new UnauthorizedException("Authentication failed. Please try again.");
        }

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> {
                    log.error("User not found after successful authentication: {}", loginRequest.getEmail());
                    return new ResourceNotFoundException("User not found");
                });

        if (user.getUserStatus() != UserStatus.ACTIVE) {
            log.warn("Login failed: account not active for {}", loginRequest.getEmail());
            throw new UnauthorizedException("Your account is not active yet. Please wait for administrator validation.");
        }

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getId(),
                user.getRoleUser().toString()
        );

        user.setOnline(true);
        userRepository.save(user);
        log.info("User logged in successfully: {}", user.getEmail());

        return new LoginResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRoleUser().toString()
        );
    }

    @Override
    public UserResponse convertToUserResponse(User user) {
        UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElse(null);
        UserResponse response = new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getDateOfBirth(),
                user.getRoleUser().toString(),
                user.getUserStatus().toString(),
            user.isOnline(),
            profile != null ? profile.getProfilePictureUrl() : null
        );
        return response;
    }

    public void setUserOffline(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setOnline(false);
            userRepository.save(user);
            log.info("User {} set to offline", email);
        });
    }

    public List<UserSearchResult> searchUsersWithProfile(String query) {
        if (query == null || query.trim().length() < 2) {
            return List.of();
        }
        String trimmed = query.trim();
        List<User> users = userRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        trimmed, trimmed, trimmed);

        return users.stream()
                .filter(u -> u.getUserStatus() == UserStatus.ACTIVE)
                .limit(10)
                .map(u -> {
                    UserProfile profile = userProfileRepository.findByUserId(u.getId()).orElse(null);
                    return new UserSearchResult(
                            u.getId(),
                            u.getFirstName(),
                            u.getLastName(),
                            u.getRoleUser().toString(),
                            profile != null ? profile.getHeadline() : null,
                            profile != null ? profile.getProfilePictureUrl() : null,
                            u.isOnline()
                    );
                })
                .collect(Collectors.toList());
    }
}