package tn.entreprise.escproject.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.entreprise.escproject.dto.EvaluationResponse;
import tn.entreprise.escproject.entite.EvaluationFormation;
import tn.entreprise.escproject.entite.Formation;
import tn.entreprise.escproject.entite.User;
import tn.entreprise.escproject.repositories.EvaluationFormationRepository;
import tn.entreprise.escproject.repositories.FormationRepository;
import tn.entreprise.escproject.repositories.InscriptionFormationRepository;
import tn.entreprise.escproject.repositories.UserRepository;
import tn.entreprise.escproject.services.Interfaces.IEvaluationFormationService;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class EvaluationFormationServiceImpl implements IEvaluationFormationService {

    private EvaluationFormationRepository evaluationRepository;
    private FormationRepository formationRepository;
    private UserRepository userRepository;
    private InscriptionFormationRepository inscriptionRepository;

    // ─── Mapper interne ───────────────────────────────────────────────────────
    private EvaluationResponse toResponse(EvaluationFormation e) {
        return new EvaluationResponse(
                e.getId(),
                e.getNote(),
                e.getCommentaire(),
                e.getDateEvaluation() != null ? e.getDateEvaluation().toString() : null,
                e.getUser() != null ? e.getUser().getId() : null,
                e.getUser() != null ? e.getUser().getName() : null,
                e.getFormation() != null ? e.getFormation().getId() : null,
                e.getFormation() != null ? e.getFormation().getTitre() : null
        );
    }

    @Override
    public EvaluationFormation addEvaluation(Long userId, Long formationId,
                                             Integer note, String commentaire) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new RuntimeException("Formation introuvable"));

        // Un utilisateur doit être inscrit pour évaluer
        if (!inscriptionRepository.existsByUser_IdAndFormation_Id(userId, formationId)) {
            throw new RuntimeException("Vous devez être inscrit pour évaluer ❌");
        }

        // Une seule évaluation par utilisateur par formation
        if (evaluationRepository.existsByUser_IdAndFormation_Id(userId, formationId)) {
            throw new RuntimeException("Vous avez déjà évalué cette formation ❌");
        }

        if (note < 1 || note > 5) {
            throw new RuntimeException("La note doit être entre 1 et 5 ❌");
        }

        EvaluationFormation evaluation = new EvaluationFormation();
        evaluation.setUser(user);
        evaluation.setFormation(formation);
        evaluation.setNote(note);
        evaluation.setCommentaire(commentaire);
        evaluation.setDateEvaluation(LocalDate.now());

        return evaluationRepository.save(evaluation);
    }

    @Override
    public List<EvaluationResponse> getEvaluationsByFormation(Long formationId) {
        return evaluationRepository.findByFormationId(formationId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public Double getMoyenneNote(Long formationId) {
        Double moyenne = evaluationRepository.findAverageNoteByFormationId(formationId);
        return moyenne != null ? moyenne : 0.0;
    }

    @Override
    public void deleteEvaluation(Long id) {
        evaluationRepository.deleteById(id);
    }
}