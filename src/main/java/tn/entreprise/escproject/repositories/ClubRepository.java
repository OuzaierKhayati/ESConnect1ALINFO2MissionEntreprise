package tn.entreprise.escproject.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import tn.entreprise.escproject.entite.Club;

import java.util.List;

@Repository
public interface ClubRepository extends CrudRepository<Club, Long> {

    List<Club> findByUserIdOrderByStartDateDesc(Long userId);
}
