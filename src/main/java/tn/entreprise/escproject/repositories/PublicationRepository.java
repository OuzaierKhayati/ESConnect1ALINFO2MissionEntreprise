package tn.entreprise.escproject.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tn.entreprise.escproject.entite.Publication;
import tn.entreprise.escproject.entite.User;

@Repository
public interface PublicationRepository extends JpaRepository<Publication, Long> {

    Page<Publication> findAllByOrderByDateCreationDesc(Pageable pageable);

    List<Publication> findByAuteurOrderByDateCreationDesc(User auteur);

    Page<Publication> findByAuteurOrderByDateCreationDesc(User auteur, Pageable pageable);

    @Query("SELECT p FROM Publication p WHERE LOWER(p.contenu) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY p.dateCreation DESC")
    Page<Publication> searchByContenu(@Param("keyword") String keyword, Pageable pageable);

    long countByAuteur(User auteur);
}