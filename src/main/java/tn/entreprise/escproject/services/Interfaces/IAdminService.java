package tn.entreprise.escproject.services.Interfaces;

import java.util.List;

import tn.entreprise.escproject.dto.AdminCreateUserRequest;
import tn.entreprise.escproject.dto.AdminUpdateUserRequest;
import tn.entreprise.escproject.dto.DashboardStatsResponse;
import tn.entreprise.escproject.dto.UserResponse;
import tn.entreprise.escproject.entite.UserStatus;

public interface IAdminService {

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);

    UserResponse createUser(AdminCreateUserRequest request);

    UserResponse updateUser(Long id, AdminUpdateUserRequest request);

    void deleteUser(Long id);

    UserResponse switchUserStatus(Long id, UserStatus userStatus);

    UserResponse switchUserRole(Long id, String role);

    List<UserResponse> searchUsers(String query);

    List<UserResponse> filterUsers(String role, String status);

    DashboardStatsResponse getDashboardStats();
}
