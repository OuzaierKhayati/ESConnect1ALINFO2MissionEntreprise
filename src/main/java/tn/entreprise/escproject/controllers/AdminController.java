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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import tn.entreprise.escproject.dto.AdminCreateUserRequest;
import tn.entreprise.escproject.dto.AdminUpdateUserRequest;
import tn.entreprise.escproject.dto.ApiResponse;
import tn.entreprise.escproject.dto.DashboardStatsResponse;
import tn.entreprise.escproject.dto.UserResponse;
import tn.entreprise.escproject.entite.UserStatus;
import tn.entreprise.escproject.services.AdminServiceImp;

@RestController
@RequestMapping("/user/admin")
public class AdminController {

    @Autowired
    private AdminServiceImp adminServiceImp;

    // --- Dashboard ---

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        DashboardStatsResponse stats = adminServiceImp.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Dashboard stats retrieved successfully", stats));
    }

    // --- User CRUD ---

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = adminServiceImp.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = adminServiceImp.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody AdminCreateUserRequest request) {
        UserResponse user = adminServiceImp.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", user));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable Long id, @RequestBody AdminUpdateUserRequest request) {
        UserResponse user = adminServiceImp.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        adminServiceImp.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }

    // --- Status & Role Management ---

    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> switchUserStatus(@PathVariable Long id, @RequestParam UserStatus userStatus) {
        UserResponse user = adminServiceImp.switchUserStatus(id, userStatus);
        return ResponseEntity.ok(ApiResponse.success("User status updated successfully", user));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<UserResponse>> switchUserRole(@PathVariable Long id, @RequestParam String role) {
        UserResponse user = adminServiceImp.switchUserRole(id, role);
        return ResponseEntity.ok(ApiResponse.success("User role updated successfully", user));
    }

    // --- Search & Filter ---

    @GetMapping("/users/search")
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsers(@RequestParam String query) {
        List<UserResponse> users = adminServiceImp.searchUsers(query);
        return ResponseEntity.ok(ApiResponse.success("Search results retrieved", users));
    }

    @GetMapping("/users/filter")
    public ResponseEntity<ApiResponse<List<UserResponse>>> filterUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {
        List<UserResponse> users = adminServiceImp.filterUsers(role, status);
        return ResponseEntity.ok(ApiResponse.success("Filtered users retrieved", users));
    }
}
