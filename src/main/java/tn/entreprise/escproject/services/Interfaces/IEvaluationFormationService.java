package tn.entreprise.escproject.services.Interfaces;

import tn.entreprise.escproject.dto.EvaluationResponse;
import tn.entreprise.escproject.entite.EvaluationFormation;

import java.util.List;

public interface IEvaluationFormationService {

    EvaluationFormation addEvaluation(Long userId, Long formationId, Integer note, String commentaire);

    List<EvaluationResponse> getEvaluationsByFormation(Long formationId);

    Double getMoyenneNote(Long formationId);

    void deleteEvaluation(Long id);
}