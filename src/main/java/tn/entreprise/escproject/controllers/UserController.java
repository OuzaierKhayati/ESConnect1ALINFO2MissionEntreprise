package tn.entreprise.escproject.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import tn.entreprise.escproject.dto.LoginRequest;
import tn.entreprise.escproject.dto.LoginResponse;
import tn.entreprise.escproject.dto.UserResponse;
import tn.entreprise.escproject.entite.User;
import tn.entreprise.escproject.services.UserServiceImp;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserServiceImp userServiceImp;

    /**
     * Register a new user
     * @param user - User object with credentials
     * @return ResponseEntity with registered user
     */
    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody User user) {
        try {
            User registeredUser = userServiceImp.registerUser(user);
            return ResponseEntity.ok(registeredUser);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Authenticate user and get JWT token
     * @param loginRequest - Contains email and password
     * @return ResponseEntity with token and user info
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = userServiceImp.authenticateUser(loginRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    /**
     * Get user by ID (requires authentication)
     * @param id - User ID
     * @return ResponseEntity with UserResponse (no password)
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        User user = userServiceImp.getById(id);
        if (user != null) {
            return ResponseEntity.ok(userServiceImp.convertToUserResponse(user));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @PostMapping("/add")
    public User addUser(@RequestBody User user){
        return userServiceImp.add(user);
    }

    @PutMapping("/update")
    public User updateUser(@RequestBody User user) {
        return userServiceImp.update(user);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteUser(@PathVariable long id) {
        userServiceImp.delete(id);
    }

    @GetMapping("/getById/{id}")
    public User getById(@PathVariable long id){
        return userServiceImp.getById(id);
    }

    @GetMapping("/getAll")
    public List<User> getAll(){
        return userServiceImp.getAll();
    }

    @PostMapping("/addAll")
    public List<User> addAll(@RequestBody List<User> users){
        return userServiceImp.addAll(users);
    }
}