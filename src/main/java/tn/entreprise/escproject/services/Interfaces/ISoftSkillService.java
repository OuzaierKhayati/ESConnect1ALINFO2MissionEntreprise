package tn.entreprise.escproject.services.Interfaces;

import tn.entreprise.escproject.entite.SoftSkill;

import java.util.List;

public interface ISoftSkillService {

    SoftSkill addSoftSkill(SoftSkill softSkill);

    List<SoftSkill> getAll();

    SoftSkill getById(Long id);

    void deleteSoftSkill(Long id);

    SoftSkill updateSoftSkill(Long id, SoftSkill softSkill);
}