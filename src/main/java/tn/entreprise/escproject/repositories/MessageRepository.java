package tn.entreprise.escproject.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.entreprise.escproject.entite.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
}