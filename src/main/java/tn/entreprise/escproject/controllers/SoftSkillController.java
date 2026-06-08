package tn.entreprise.escproject.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.entreprise.escproject.entite.SoftSkill;
import tn.entreprise.escproject.services.Interfaces.ISoftSkillService;

import java.util.List;

@RestController
@RequestMapping("/softskills")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class SoftSkillController {

    private ISoftSkillService softSkillService;

    @PostMapping
    public ResponseEntity<SoftSkill> add(@RequestBody SoftSkill softSkill) {
        return ResponseEntity.ok(softSkillService.addSoftSkill(softSkill));
    }

    @GetMapping
    public ResponseEntity<List<SoftSkill>> getAll() {
        return ResponseEntity.ok(softSkillService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SoftSkill> getById(@PathVariable Long id) {
        return ResponseEntity.ok(softSkillService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SoftSkill> update(@PathVariable Long id,
                                            @RequestBody SoftSkill softSkill) {
        return ResponseEntity.ok(softSkillService.updateSoftSkill(id, softSkill));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        softSkillService.deleteSoftSkill(id);
        return ResponseEntity.noContent().build();
    }
}