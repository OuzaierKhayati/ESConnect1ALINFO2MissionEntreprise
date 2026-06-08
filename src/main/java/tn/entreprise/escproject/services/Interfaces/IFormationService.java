package tn.entreprise.escproject.services.Interfaces;

import tn.entreprise.escproject.dto.FormationResponse;
import tn.entreprise.escproject.entite.Formation;

import java.util.List;

public interface IFormationService {

    Formation addFormation(Formation formation);

    List<FormationResponse> getAllFormations();

    List<FormationResponse> getFormationsPubliees();

    Formation getFormationById(Long id);

    FormationResponse getFormationResponseById(Long id);

    Formation updateFormation(Formation formation);

    FormationResponse updateFormationResponse(Formation formation);

    void deleteFormation(Long id);

    List<FormationResponse> getFormationsByFormateur(Long createurId);

    List<FormationResponse> searchFormations(String keyword);

    List<FormationResponse> getFormationsByCategorie(String categorie);

    Formation publierFormation(Long id);

    FormationResponse publierFormationResponse(Long id);

    Formation depublierFormation(Long id);

    FormationResponse depublierFormationResponse(Long id);
}