package tn.entreprise.escproject.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tn.entreprise.escproject.entite.PasswordResetToken;
import tn.entreprise.escproject.entite.TokenStatus;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByTokenAndStatus(String token, TokenStatus status);
}
