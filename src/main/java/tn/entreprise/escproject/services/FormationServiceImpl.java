package tn.entreprise.escproject.services;

import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import tn.entreprise.escproject.dto.FormationResponse;
import tn.entreprise.escproject.entite.Formation;
import tn.entreprise.escproject.entite.StatutInscription;
import tn.entreprise.escproject.entite.User;
import tn.entreprise.escproject.repositories.EvaluationFormationRepository;
import tn.entreprise.escproject.repositories.FormationRepository;
import tn.entreprise.escproject.repositories.InscriptionFormationRepository;
import tn.entreprise.escproject.repositories.UserRepository;
import tn.entreprise.escproject.services.Interfaces.IFormationService;

import java.util.List;

@Service
@AllArgsConstructor
public class FormationServiceImpl implements IFormationService {

    private FormationRepository formationRepository;
    private UserRepository userRepository;
    private InscriptionFormationRepository inscriptionFormationRepository;
    private EvaluationFormationRepository evaluationFormationRepository;


    private FormationResponse toResponse(Formation f) {
        long inscrits = inscriptionFormationRepository.countByFormationIdAndStatutNot(f.getId(), StatutInscription.ANNULE)
            - inscriptionFormationRepository.findByFormationIdAndStatut(f.getId(), StatutInscription.REJETE).size();
        long placesRestantes = 0;
        if (f.getCapaciteMax() != null) {
            placesRestantes = f.getCapaciteMax() - inscrits;
            if (placesRestantes < 0) {
                placesRestantes = 0;
            }
            if (placesRestantes > f.getCapaciteMax()) {
                placesRestantes = f.getCapaciteMax();
            }
        }
        Double moyenne = evaluationFormationRepository.findAverageNoteByFormationId(f.getId());

        List<String> softSkillNoms = List.of();
        try {
            if (f.getSoftSkills() != null) {
                softSkillNoms = f.getSoftSkills().stream()
                        .map(s -> s.getNom())
                        .toList();
            }
        } catch (Exception e) {
            softSkillNoms = List.of();
        }

        return new FormationResponse(
                f.getId(),
                f.getTitre(),
                f.getDescription(),
                f.getDate() != null ? f.getDate().toString() : null,
                f.getHeure(),
                f.getDuree(),
                f.getMode(),
                f.getEmplacement(),
                f.getCapaciteMax(),
                f.getPrix(),
                f.getGratuite(),
                f.getImage(),
                f.getLienReunion(),
                f.getCategorie(),
                f.getNiveau(),
                f.getPubliee(),
                f.getCreateur() != null ? f.getCreateur().getId() : null,
                f.getCreateur() != null ? f.getCreateur().getFirstName() : null,
                placesRestantes,
                moyenne,
                softSkillNoms
        );
    }

    @Override
    public Formation addFormation(Formation formation) {
        Long userId = formation.getCreateur().getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Formateur introuvable"));
        formation.setCreateur(user);
        if (formation.getPubliee() == null) formation.setPubliee(false);
        return formationRepository.save(formation);
    }

    @Override
    public List<FormationResponse> getAllFormations() {
        return formationRepository.findAllWithSoftSkills()
                .stream().map(this::toResponse).toList();
    }

    @Override
    public List<FormationResponse> getFormationsPubliees() {
        return formationRepository.findByPublieeTrue()
                .stream().map(this::toResponse).toList();
    }

    @Override
    public Formation getFormationById(Long id) {
        return formationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formation introuvable"));
    }

    @Override
    public Formation updateFormation(Formation formation) {
        Long userId = formation.getCreateur().getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Formateur introuvable"));
        formation.setCreateur(user);
        return formationRepository.save(formation);
    }

    @Override
    @Transactional
    public void deleteFormation(Long id) {
        Formation formation = formationRepository.findByIdWithSoftSkills(id)
                .orElseThrow(() -> new RuntimeException("Formation introuvable"));

        evaluationFormationRepository.deleteByFormationId(id);
        inscriptionFormationRepository.deleteByFormationId(id);

        if (formation.getSoftSkills() != null && !formation.getSoftSkills().isEmpty()) {
            formation.getSoftSkills().clear();
            formationRepository.save(formation);
        }

        formationRepository.delete(formation);
    }


    @Override
    public List<FormationResponse> getFormationsByFormateur(Long createurId) {
        return formationRepository.findByCreateurId(createurId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public List<FormationResponse> searchFormations(String keyword) {
        return formationRepository.findByTitreContainingIgnoreCase(keyword)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public List<FormationResponse> getFormationsByCategorie(String categorie) {
        if (categorie == null || categorie.isBlank()) {
            return getFormationsPubliees();
        }

        String normalized = categorie.trim();
        if (normalized.equalsIgnoreCase("toutes les catégories")
                || normalized.equalsIgnoreCase("toutes les categories")) {
            return getFormationsPubliees();
        }

        return formationRepository.findPublishedByCategorieIgnoreCase(normalized)
                .stream().map(this::toResponse).toList();
    }


    @Override
    public Formation publierFormation(Long id) {
        Formation f = formationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formation introuvable"));
        f.setPubliee(true);
        return formationRepository.save(f);
    }

    @Override
    public Formation depublierFormation(Long id) {
        Formation f = formationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formation introuvable"));
        f.setPubliee(false);
        return formationRepository.save(f);
    }
    @Override
    public FormationResponse getFormationResponseById(Long id) {
        Formation f = formationRepository.findByIdWithSoftSkills(id)
                .orElseThrow(() -> new RuntimeException("Formation introuvable"));
        return toResponse(f);
    }

    @Override
    public FormationResponse updateFormationResponse(Formation formation) {
        Formation updated = updateFormation(formation);
        return getFormationResponseById(updated.getId());
    }

    @Override
    public FormationResponse publierFormationResponse(Long id) {
        Formation f = formationRepository.findByIdWithSoftSkills(id)
                .orElseThrow(() -> new RuntimeException("Formation introuvable"));
        f.setPubliee(true);
        formationRepository.save(f);
        return toResponse(f);
    }

    @Override
    public FormationResponse depublierFormationResponse(Long id) {
        Formation f = formationRepository.findByIdWithSoftSkills(id)
                .orElseThrow(() -> new RuntimeException("Formation introuvable"));
        f.setPubliee(false);
        formationRepository.save(f);
        return toResponse(f);
    }
}