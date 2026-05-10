package tn.entreprise.escproject.services.Interfaces;

import tn.entreprise.escproject.dto.LoginRequest;
import tn.entreprise.escproject.dto.LoginResponse;
import tn.entreprise.escproject.dto.RegisterRequest;
import tn.entreprise.escproject.dto.UserResponse;
import tn.entreprise.escproject.entite.User;

public interface IUserService {
    UserResponse registerUser(RegisterRequest registerRequest);
    LoginResponse authenticateUser(LoginRequest loginRequest);
    UserResponse convertToUserResponse(User user);
}
