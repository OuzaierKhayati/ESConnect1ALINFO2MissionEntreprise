package tn.entreprise.escproject.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import tn.entreprise.escproject.entite.Certification;

import java.util.List;

@Repository
public interface CertificationRepository extends CrudRepository<Certification, Long> {

    List<Certification> findByUserIdOrderByIssueDateDesc(Long userId);
}
