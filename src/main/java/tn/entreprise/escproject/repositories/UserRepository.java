package tn.entreprise.escproject.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import tn.entreprise.escproject.entite.RoleUser;
import tn.entreprise.escproject.entite.User;
import tn.entreprise.escproject.entite.UserStatus;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByUserStatus(UserStatus userStatus);

    List<User> findByRoleUser(RoleUser roleUser);

    List<User> findByRoleUserAndUserStatus(RoleUser roleUser, UserStatus userStatus);

    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String firstName, String lastName, String email);

    long countByUserStatus(UserStatus userStatus);

    long countByRoleUser(RoleUser roleUser);
}
