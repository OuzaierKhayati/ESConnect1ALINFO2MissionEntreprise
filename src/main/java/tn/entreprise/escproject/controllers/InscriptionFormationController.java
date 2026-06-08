package tn.entreprise.escproject.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.entreprise.escproject.dto.InscriptionResponse;
import tn.entreprise.escproject.entite.InscriptionFormation;
import tn.entreprise.escproject.entite.StatutInscription;
import tn.entreprise.escproject.services.Interfaces.IInscriptionFormationService;

import java.util.List;

@RestController
@RequestMapping("/inscriptions")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class InscriptionFormationController {

    private IInscriptionFormationService inscriptionService;

    // ─── S'inscrire à une formation ───────────────────────────────────────────

    @PostMapping("/user/{userId}/formation/{formationId}")
    public ResponseEntity<InscriptionFormation> inscrire(
            @PathVariable Long userId,
            @PathVariable Long formationId) {
        return ResponseEntity.ok(inscriptionService.addInscription(userId, formationId));
    }

    // ─── Annuler son inscription ──────────────────────────────────────────────

    @PutMapping("/annuler/user/{userId}/formation/{formationId}")
    public ResponseEntity<Void> annuler(
            @PathVariable Long userId,
            @PathVariable Long formationId) {
        inscriptionService.annulerInscription(userId, formationId);
        return ResponseEntity.noContent().build();
    }

    // ─── Liste des inscrits d'une formation (pour le formateur) ───────────────

    @GetMapping("/formation/{formationId}")
    public ResponseEntity<List<InscriptionResponse>> getByFormation(
            @PathVariable Long formationId) {
        return ResponseEntity.ok(inscriptionService.getInscriptionsByFormation(formationId));
    }

    // ─── Formations d'un utilisateur ──────────────────────────────────────────

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<InscriptionResponse>> getByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(inscriptionService.getInscriptionsByUser(userId));
    }

    // ─── Changer le statut (admin / formateur) ────────────────────────────────

    @PutMapping("/{id}/statut")
    public ResponseEntity<InscriptionFormation> updateStatut(
            @PathVariable Long id,
            @RequestParam StatutInscription statut) {
        return ResponseEntity.ok(inscriptionService.updateStatut(id, statut));
    }

    // ─── Supprimer une inscription ────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        inscriptionService.deleteInscription(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Toutes les inscriptions (admin) ──────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<InscriptionFormation>> getAll() {
        return ResponseEntity.ok(inscriptionService.getAll());
    }
}