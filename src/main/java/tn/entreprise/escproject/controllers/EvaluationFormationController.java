package tn.entreprise.escproject.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.entreprise.escproject.dto.EvaluationResponse;
import tn.entreprise.escproject.entite.EvaluationFormation;
import tn.entreprise.escproject.services.Interfaces.IEvaluationFormationService;

import java.util.List;

@RestController
@RequestMapping("/evaluations")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class EvaluationFormationController {

    private IEvaluationFormationService evaluationService;

    // ─── Ajouter une évaluation ───────────────────────────────────────────────

    @PostMapping("/user/{userId}/formation/{formationId}")
    public ResponseEntity<EvaluationFormation> addEvaluation(
            @PathVariable Long userId,
            @PathVariable Long formationId,
            @RequestParam Integer note,
            @RequestParam(required = false) String commentaire) {
        return ResponseEntity.ok(
                evaluationService.addEvaluation(userId, formationId, note, commentaire)
        );
    }

    // ─── Évaluations d'une formation ──────────────────────────────────────────

    @GetMapping("/formation/{formationId}")
    public ResponseEntity<List<EvaluationResponse>> getByFormation(
            @PathVariable Long formationId) {
        return ResponseEntity.ok(evaluationService.getEvaluationsByFormation(formationId));
    }

    // ─── Moyenne d'une formation ──────────────────────────────────────────────

    @GetMapping("/formation/{formationId}/moyenne")
    public ResponseEntity<Double> getMoyenne(@PathVariable Long formationId) {
        return ResponseEntity.ok(evaluationService.getMoyenneNote(formationId));
    }

    // ─── Supprimer une évaluation (admin) ─────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        evaluationService.deleteEvaluation(id);
        return ResponseEntity.noContent().build();
    }
}