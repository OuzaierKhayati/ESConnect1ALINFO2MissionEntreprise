package tn.entreprise.escproject.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tn.entreprise.escproject.entite.Like;
import tn.entreprise.escproject.entite.Publication;
import tn.entreprise.escproject.entite.User;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    boolean existsByUserAndPublication(User user, Publication publication);

    Optional<Like> findByUserAndPublication(User user, Publication publication);

    long countByPublication(Publication publication);

    List<Like> findByPublication(Publication publication);
}