package tn.entreprise.escproject.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import tn.entreprise.escproject.entite.User;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
        Optional<User> findByEmail(String email);
}
