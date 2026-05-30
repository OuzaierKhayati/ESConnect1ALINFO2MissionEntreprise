package tn.entreprise.escproject.services.Interfaces;

import tn.entreprise.escproject.entite.DemandeFormateur;

import java.util.List;

public interface IDemandeFormateurService {

    DemandeFormateur soumettredemande(Long userId, String motif);

    List<DemandeFormateur> getDemandesEnAttente();

    DemandeFormateur approuverDemande(Long demandeId);

    DemandeFormateur rejeterDemande(Long demandeId);

    List<DemandeFormateur> getAllDemandes();
}