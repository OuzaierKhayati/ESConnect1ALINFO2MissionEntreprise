package tn.entreprise.escproject.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import tn.entreprise.escproject.dto.AdminCreateUserRequest;
import tn.entreprise.escproject.dto.AdminUpdateUserRequest;
import tn.entreprise.escproject.dto.DashboardStatsResponse;
import tn.entreprise.escproject.dto.UserResponse;
import tn.entreprise.escproject.entite.RoleUser;
import tn.entreprise.escproject.entite.User;
import tn.entreprise.escproject.entite.UserStatus;
import tn.entreprise.escproject.exception.BadRequestException;
import tn.entreprise.escproject.exception.ConflictException;
import tn.entreprise.escproject.exception.ResourceNotFoundException;
import tn.entreprise.escproject.repositories.UserRepository;
import tn.entreprise.escproject.services.Interfaces.IAdminService;

@Service
public class AdminServiceImp implements IAdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminServiceImp.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserServiceImp userServiceImp;

    @Override
    public List<UserResponse> getAllUsers() {
        log.info("Admin: fetching all users");
        return StreamSupport.stream(userRepository.findAll().spliterator(), false)
                .map(userServiceImp::convertToUserResponse)
                .toList();
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = findUserOrThrow(id);
        return userServiceImp.convertToUserResponse(user);
    }

    @Override
    public UserResponse createUser(AdminCreateUserRequest request) {
        log.info("Admin: creating user with email: {}", request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("An account with this email already exists");
        }

        RoleUser roleUser = parseRole(request.getRoleUser());
        UserStatus status = request.getUserStatus() != null
                ? parseStatus(request.getUserStatus())
                : UserStatus.ACTIVE;

        User user = new User(
                null,
                request.getEmail(),
                Objects.requireNonNull(passwordEncoder.encode(request.getPassword())),
                request.getFirstName(),
                request.getLastName(),
                request.getDateOfBirth(),
                roleUser,
                status,
                null,
                null,
                null,
                null
        );

        userRepository.save(user);
        log.info("Admin: user created successfully - {} with role {}", user.getEmail(), roleUser);
        return userServiceImp.convertToUserResponse(user);
    }

    @Override
    public UserResponse updateUser(Long id, AdminUpdateUserRequest request) {
        User user = findUserOrThrow(id);
        log.info("Admin: updating user id: {}", id);

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new ConflictException("An account with this email already exists");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getRoleUser() != null) {
            user.setRoleUser(parseRole(request.getRoleUser()));
        }
        if (request.getUserStatus() != null) {
            user.setUserStatus(parseStatus(request.getUserStatus()));
        }

        userRepository.save(user);
        log.info("Admin: user updated successfully - id: {}", id);
        return userServiceImp.convertToUserResponse(user);
    }

    @Override
    public void deleteUser(Long id) {
        User user = findUserOrThrow(id);

        if (user.getRoleUser() == RoleUser.ADMIN) {
            long adminCount = userRepository.countByRoleUser(RoleUser.ADMIN);
            if (adminCount <= 1) {
                throw new BadRequestException("Cannot delete the last admin account");
            }
        }

        userRepository.deleteById(id);
        log.info("Admin: user deleted - id: {}", id);
    }

    @Override
    public UserResponse switchUserStatus(Long id, UserStatus userStatus) {
        User user = findUserOrThrow(id);
        user.setUserStatus(userStatus);
        userRepository.save(user);
        log.info("Admin: user status changed to {} for id: {}", userStatus, id);
        return userServiceImp.convertToUserResponse(user);
    }

    @Override
    public UserResponse switchUserRole(Long id, String role) {
        User user = findUserOrThrow(id);
        RoleUser newRole = parseRole(role);

        if (user.getRoleUser() == RoleUser.ADMIN && newRole != RoleUser.ADMIN) {
            long adminCount = userRepository.countByRoleUser(RoleUser.ADMIN);
            if (adminCount <= 1) {
                throw new BadRequestException("Cannot change role of the last admin account");
            }
        }

        user.setRoleUser(newRole);
        userRepository.save(user);
        log.info("Admin: user role changed to {} for id: {}", newRole, id);
        return userServiceImp.convertToUserResponse(user);
    }

    @Override
    public List<UserResponse> searchUsers(String query) {
        log.info("Admin: searching users with query: {}", query);
        return userRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query, query)
                .stream()
                .map(userServiceImp::convertToUserResponse)
                .toList();
    }

    @Override
    public List<UserResponse> filterUsers(String role, String status) {
        log.info("Admin: filtering users - role: {}, status: {}", role, status);

        if (role != null && status != null) {
            return userRepository.findByRoleUserAndUserStatus(parseRole(role), parseStatus(status))
                    .stream().map(userServiceImp::convertToUserResponse).toList();
        } else if (role != null) {
            return userRepository.findByRoleUser(parseRole(role))
                    .stream().map(userServiceImp::convertToUserResponse).toList();
        } else if (status != null) {
            return userRepository.findByUserStatus(parseStatus(status))
                    .stream().map(userServiceImp::convertToUserResponse).toList();
        }

        return getAllUsers();
    }

    @Override
    public DashboardStatsResponse getDashboardStats() {
        log.info("Admin: fetching dashboard stats");

        long total = userRepository.count();
        long active = userRepository.countByUserStatus(UserStatus.ACTIVE);
        long pending = userRepository.countByUserStatus(UserStatus.PENDING);
        long inactive = userRepository.countByUserStatus(UserStatus.INACTIVE);

        Map<String, Long> usersByRole = new HashMap<>();
        for (RoleUser role : RoleUser.values()) {
            usersByRole.put(role.name(), userRepository.countByRoleUser(role));
        }

        return new DashboardStatsResponse(total, active, pending, inactive, usersByRole);
    }

    // --- Private helpers ---

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Admin: user not found with id: {}", id);
                    return new ResourceNotFoundException("User not found with id: " + id);
                });
    }

    private RoleUser parseRole(String role) {
        try {
            return RoleUser.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role: " + role);
        }
    }

    private UserStatus parseStatus(String status) {
        try {
            return UserStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + status);
        }
    }
}
