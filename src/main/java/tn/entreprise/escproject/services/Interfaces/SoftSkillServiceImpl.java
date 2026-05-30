package tn.entreprise.escproject.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.entreprise.escproject.entite.SoftSkill;
import tn.entreprise.escproject.repositories.SoftSkillRepository;
import tn.entreprise.escproject.services.Interfaces.ISoftSkillService;

import java.util.List;

@Service
@AllArgsConstructor
public class SoftSkillServiceImpl implements ISoftSkillService {

    private SoftSkillRepository softSkillRepository;

    @Override
    public SoftSkill addSoftSkill(SoftSkill softSkill) {
        if (softSkillRepository.existsByNom(softSkill.getNom())) {
            throw new RuntimeException("Ce softskill existe déjà ❌");
        }
        return softSkillRepository.save(softSkill);
    }

    @Override
    public List<SoftSkill> getAll() {
        return softSkillRepository.findAll();
    }

    @Override
    public SoftSkill getById(Long id) {
        return softSkillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SoftSkill introuvable"));
    }

    @Override
    public void deleteSoftSkill(Long id) {
        softSkillRepository.deleteById(id);
    }

    @Override
    public SoftSkill updateSoftSkill(Long id, SoftSkill softSkill) {
        SoftSkill existing = softSkillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SoftSkill introuvable"));
        existing.setNom(softSkill.getNom());
        existing.setDescription(softSkill.getDescription());
        return softSkillRepository.save(existing);
    }
}