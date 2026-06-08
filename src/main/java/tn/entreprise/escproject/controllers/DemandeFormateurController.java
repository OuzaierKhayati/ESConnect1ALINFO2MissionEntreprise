package tn.entreprise.escproject.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.entreprise.escproject.entite.DemandeFormateur;
import tn.entreprise.escproject.services.Interfaces.IDemandeFormateurService;

import java.util.List;

@RestController
@RequestMapping("/demandes-formateur")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class DemandeFormateurController {

    private IDemandeFormateurService demandeService;

    // ─── Utilisateur soumet une demande ───────────────────────────────────────

    @PostMapping("/user/{userId}")
    public ResponseEntity<DemandeFormateur> soumettre(
            @PathVariable Long userId,
            @RequestParam(required = false) String motif) {
        return ResponseEntity.ok(demandeService.soumettredemande(userId, motif));
    }

    // ─── Admin : voir toutes les demandes ─────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<DemandeFormateur>> getAll() {
        return ResponseEntity.ok(demandeService.getAllDemandes());
    }

    // ─── Admin : voir demandes en attente ─────────────────────────────────────

    @GetMapping("/en-attente")
    public ResponseEntity<List<DemandeFormateur>> getEnAttente() {
        return ResponseEntity.ok(demandeService.getDemandesEnAttente());
    }

    // ─── Admin : approuver une demande ────────────────────────────────────────

    @PutMapping("/{id}/approuver")
    public ResponseEntity<DemandeFormateur> approuver(@PathVariable Long id) {
        return ResponseEntity.ok(demandeService.approuverDemande(id));
    }

    // ─── Admin : rejeter une demande ──────────────────────────────────────────

    @PutMapping("/{id}/rejeter")
    public ResponseEntity<DemandeFormateur> rejeter(@PathVariable Long id) {
        return ResponseEntity.ok(demandeService.rejeterDemande(id));
    }
}