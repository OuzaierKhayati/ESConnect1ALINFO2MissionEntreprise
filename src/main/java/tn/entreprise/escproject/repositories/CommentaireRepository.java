package tn.entreprise.escproject.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tn.entreprise.escproject.entite.Commentaire;
import tn.entreprise.escproject.entite.Publication;
import tn.entreprise.escproject.entite.User;

@Repository
public interface CommentaireRepository extends JpaRepository<Commentaire, Long> {

    List<Commentaire> findByPublicationOrderByDateCreationAsc(Publication publication);

    Page<Commentaire> findByPublicationOrderByDateCreationDesc(Publication publication, Pageable pageable);

    List<Commentaire> findByAuteurOrderByDateCreationDesc(User auteur);

    long countByPublication(Publication publication);
}