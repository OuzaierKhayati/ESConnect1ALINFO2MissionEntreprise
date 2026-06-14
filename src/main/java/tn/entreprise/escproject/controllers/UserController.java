package tn.entreprise.escproject.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import tn.entreprise.escproject.dto.ApiResponse;
import tn.entreprise.escproject.dto.LoginRequest;
import tn.entreprise.escproject.dto.LoginResponse;
import tn.entreprise.escproject.dto.RegisterRequest;
import tn.entreprise.escproject.dto.UpdateOwnProfileRequest;
import tn.entreprise.escproject.dto.UserResponse;
import tn.entreprise.escproject.dto.UserSearchResult;
import tn.entreprise.escproject.entite.User;
import tn.entreprise.escproject.exception.ResourceNotFoundException;
import tn.entreprise.escproject.services.UserServiceImp;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserServiceImp userServiceImp;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        UserResponse registeredUser = userServiceImp.registerUser(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration completed successfully", registeredUser));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = userServiceImp.authenticateUser(loginRequest);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            userServiceImp.setUserOffline(userDetails.getUsername());
        }
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateOwnProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateOwnProfileRequest request) {
        UserResponse response = userServiceImp.updateOwnProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        User user = userServiceImp.getById(id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", userServiceImp.convertToUserResponse(user)));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<UserResponse>> addUser(@RequestBody User user) {
        User saved = userServiceImp.add(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", userServiceImp.convertToUserResponse(saved)));
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@RequestBody User user) {
        User updated = userServiceImp.update(user);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", userServiceImp.convertToUserResponse(updated)));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userServiceImp.delete(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Long id) {
        User user = userServiceImp.getById(id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", userServiceImp.convertToUserResponse(user)));
    }

    @GetMapping("/getAll")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAll() {
        List<UserResponse> users = userServiceImp.getAll().stream()
                .map(userServiceImp::convertToUserResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @PostMapping("/addAll")
    public ResponseEntity<ApiResponse<List<UserResponse>>> addAll(@RequestBody List<User> users) {
        List<UserResponse> savedUsers = userServiceImp.addAll(users).stream()
                .map(userServiceImp::convertToUserResponse)
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Users created successfully", savedUsers));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserSearchResult>>> searchUsers(@RequestParam String query) {
        List<UserSearchResult> results = userServiceImp.searchUsersWithProfile(query);
        return ResponseEntity.ok(ApiResponse.success("Search results", results));
    }
}