package tn.entreprise.escproject.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.entreprise.escproject.dto.FormationResponse;
import tn.entreprise.escproject.entite.Formation;
import tn.entreprise.escproject.services.Interfaces.IFormationService;

import java.util.List;

@RestController
@RequestMapping("/formations")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class FormationController {

    private IFormationService formationService;

    @PostMapping
    public ResponseEntity<Formation> addFormation(@RequestBody Formation formation) {
        return ResponseEntity.ok(formationService.addFormation(formation));
    }

    @GetMapping
    public ResponseEntity<List<FormationResponse>> getAllFormations() {
        return ResponseEntity.ok(formationService.getAllFormations());
    }

    @GetMapping("/publiees")
    public ResponseEntity<List<FormationResponse>> getFormationsPubliees() {
        return ResponseEntity.ok(formationService.getFormationsPubliees());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FormationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(formationService.getFormationResponseById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FormationResponse> update(@PathVariable Long id,
                                                    @RequestBody Formation formation) {
        formation.setId(id);
        return ResponseEntity.ok(formationService.updateFormationResponse(formation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        formationService.deleteFormation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/formateur/{createurId}")
    public ResponseEntity<List<FormationResponse>> getByFormateur(@PathVariable Long createurId) {
        return ResponseEntity.ok(formationService.getFormationsByFormateur(createurId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<FormationResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(formationService.searchFormations(keyword));
    }

    @GetMapping("/categorie/{categorie}")
    public ResponseEntity<List<FormationResponse>> getByCategorie(@PathVariable String categorie) {
        return ResponseEntity.ok(formationService.getFormationsByCategorie(categorie));
    }

    @PutMapping("/{id}/publier")
    public ResponseEntity<FormationResponse> publier(@PathVariable Long id) {
        return ResponseEntity.ok(formationService.publierFormationResponse(id));
    }

    @PutMapping("/{id}/depublier")
    public ResponseEntity<FormationResponse> depublier(@PathVariable Long id) {
        return ResponseEntity.ok(formationService.depublierFormationResponse(id));
    }
}