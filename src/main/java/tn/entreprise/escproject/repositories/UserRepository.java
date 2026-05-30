package tn.entreprise.escproject.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.entreprise.escproject.entite.User;

public interface UserRepository extends JpaRepository<User, Long> {
}