package tn.entreprise.escproject.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.entreprise.escproject.entite.User;
import tn.entreprise.escproject.repositories.UserRepository;
import tn.entreprise.escproject.services.Interfaces.IUserService;

import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements IUserService {

    private UserRepository userRepository;

    @Override
    public User addUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User updateUser(Long id, User user) {

        User u = userRepository.findById(id).orElse(null);

        if (u != null) {
            u.setName(user.getName());
            u.setEmail(user.getEmail());
            u.setRole(user.getRole());

            return userRepository.save(u);
        }

        return null;
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}