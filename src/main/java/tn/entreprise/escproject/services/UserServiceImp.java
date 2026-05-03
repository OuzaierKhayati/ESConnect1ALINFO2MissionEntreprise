package tn.entreprise.escproject.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import tn.entreprise.escproject.dto.LoginRequest;
import tn.entreprise.escproject.dto.LoginResponse;
import tn.entreprise.escproject.dto.UserResponse;
import tn.entreprise.escproject.entite.User;
import tn.entreprise.escproject.repositories.UserRepository;
import tn.entreprise.escproject.services.Interfaces.IService;
import tn.entreprise.escproject.services.Interfaces.IUserService;
import tn.entreprise.escproject.utils.JwtUtil;

@Service
public class UserServiceImp implements IService<User>, IUserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public User add(User user) {
        // Hash password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public User update(User user) {
        return userRepository.save(user);
    }

    @Override
    public void delete(Long id) {
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
        // Hash password for each user before saving
        users.forEach(user -> user.setPassword(passwordEncoder.encode(user.getPassword())));
        return (List<User>) userRepository.saveAll(users);
    }
    
    /**
     * Register a new user
     * @param user - User object with email, password, firstName, lastName, etc.
     * @return User object saved with hashed password
     */
    public User registerUser(User user) {
        // Check if user already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User already exists with email: " + user.getEmail());
        }
        
        // Hash password and save
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
    
    /**
     * Authenticate user and generate JWT token
     * @param loginRequest - Contains email and password
     * @return LoginResponse with JWT token and user info
     */
    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        try {
            // Authenticate using Spring Security's AuthenticationManager
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
            
            // If authentication succeeds, get user details
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Generate JWT token
            String token = jwtUtil.generateToken(
                    user.getEmail(),
                    user.getId(),
                    user.getRoleUser().toString()
            );
            
            // Return response with token and user info
            return new LoginResponse(
                    token,
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRoleUser().toString()
            );
            
        } catch (Exception ex) {
            throw new RuntimeException("Authentication failed: " + ex.getMessage());
        }
    }
    
    /**
     * Convert User entity to UserResponse DTO (safe for API response)
     * @param user - User entity
     * @return UserResponse DTO without password
     */
    public UserResponse convertToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getDateOfBirth(),
                user.getRoleUser().toString()
        );
    }
}