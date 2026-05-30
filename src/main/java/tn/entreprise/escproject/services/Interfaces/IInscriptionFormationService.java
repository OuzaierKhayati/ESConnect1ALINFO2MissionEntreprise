package tn.entreprise.escproject.services.Interfaces;

import tn.entreprise.escproject.dto.InscriptionResponse;
import tn.entreprise.escproject.entite.InscriptionFormation;
import tn.entreprise.escproject.entite.StatutInscription;

import java.util.List;

public interface IInscriptionFormationService {

    InscriptionFormation addInscription(Long userId, Long formationId);

    List<InscriptionResponse> getInscriptionsByFormation(Long formationId);

    List<InscriptionResponse> getInscriptionsByUser(Long userId);

    void annulerInscription(Long userId, Long formationId);

    void deleteInscription(Long id);

    InscriptionFormation updateStatut(Long id, StatutInscription statut);

    List<InscriptionFormation> getAll();
}