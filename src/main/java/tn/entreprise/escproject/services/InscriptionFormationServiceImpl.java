package tn.entreprise.escproject.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.entreprise.escproject.dto.InscriptionResponse;
import tn.entreprise.escproject.entite.Formation;
import tn.entreprise.escproject.entite.InscriptionFormation;
import tn.entreprise.escproject.entite.StatutInscription;
import tn.entreprise.escproject.entite.User;
import tn.entreprise.escproject.exception.BadRequestException;
import tn.entreprise.escproject.exception.ConflictException;
import tn.entreprise.escproject.repositories.FormationRepository;
import tn.entreprise.escproject.repositories.InscriptionFormationRepository;
import tn.entreprise.escproject.repositories.UserRepository;
import tn.entreprise.escproject.services.Interfaces.IInscriptionFormationService;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class InscriptionFormationServiceImpl implements IInscriptionFormationService {

    private InscriptionFormationRepository inscriptionRepository;
    private FormationRepository formationRepository;
    private UserRepository userRepository;

    // ─── Mapper interne ───────────────────────────────────────────────────────
    private InscriptionResponse toResponse(InscriptionFormation i) {
        return new InscriptionResponse(
                i.getId(),
                i.getDateInscription() != null ? i.getDateInscription().toString() : null,
                i.getStatut(),
                i.getUser() != null ? i.getUser().getId() : null,
                i.getUser() != null ? i.getUser().getFirstName() : null,
                i.getUser() != null ? i.getUser().getEmail() : null,
                i.getFormation() != null ? i.getFormation().getId() : null,
                i.getFormation() != null ? i.getFormation().getTitre() : null
        );
    }

    @Override
    public InscriptionFormation addInscription(Long userId, Long formationId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new RuntimeException("Formation introuvable"));

        if (inscriptionRepository.existsByUser_IdAndFormation_IdAndStatutNot(userId, formationId, StatutInscription.ANNULE)) {
            throw new ConflictException("Vous êtes déjà inscrit à cette formation.");
        }

        long count = inscriptionRepository.countByFormationIdAndStatutNot(formationId, StatutInscription.ANNULE);
        if (formation.getCapaciteMax() != null && count >= formation.getCapaciteMax()) {
            throw new BadRequestException("Formation complète.");
        }

        InscriptionFormation inscription = new InscriptionFormation();
        inscription.setUser(user);
        inscription.setFormation(formation);
        inscription.setDateInscription(LocalDate.now());
        inscription.setStatut(StatutInscription.EN_ATTENTE);

        return inscriptionRepository.save(inscription);
    }

    @Override
    public List<InscriptionResponse> getInscriptionsByFormation(Long formationId) {
        return inscriptionRepository.findByFormationId(formationId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public List<InscriptionResponse> getInscriptionsByUser(Long userId) {
        return inscriptionRepository.findByUserId(userId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public void annulerInscription(Long userId, Long formationId) {
        InscriptionFormation inscription = inscriptionRepository
                .findByUser_IdAndFormation_Id(userId, formationId)
                .orElseThrow(() -> new RuntimeException("Inscription introuvable"));
        inscription.setStatut(StatutInscription.ANNULE);
        inscriptionRepository.save(inscription);
    }

    @Override
    public void deleteInscription(Long id) {
        inscriptionRepository.deleteById(id);
    }

    @Override
    public InscriptionFormation updateStatut(Long id, StatutInscription statut) {
        InscriptionFormation inscription = inscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscription introuvable"));
        inscription.setStatut(statut);
        return inscriptionRepository.save(inscription);
    }

    @Override
    public List<InscriptionFormation> getAll() {
        return inscriptionRepository.findAll();
    }
}