package tn.entreprise.escproject.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.entreprise.escproject.entite.DemandeFormateur;
import tn.entreprise.escproject.entite.User;
import tn.entreprise.escproject.entite.RoleUser;
import tn.entreprise.escproject.repositories.DemandeFormateurRepository;
import tn.entreprise.escproject.repositories.UserRepository;
import tn.entreprise.escproject.services.Interfaces.IDemandeFormateurService;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class DemandeFormateurServiceImpl implements IDemandeFormateurService {

    private DemandeFormateurRepository demandeRepository;
    private UserRepository userRepository;

    @Override
    public DemandeFormateur soumettredemande(Long userId, String motif) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (demandeRepository.existsByUser_IdAndStatut(userId, "EN_ATTENTE")) {
            throw new RuntimeException("Vous avez déjà une demande en attente ❌");
        }

        if (RoleUser.FORMATEUR.equals(user.getRoleUser())) {
            throw new RuntimeException("Vous êtes déjà formateur ❌");
        }

        DemandeFormateur demande = new DemandeFormateur();
        demande.setUser(user);
        demande.setMotif(motif);
        demande.setStatut("EN_ATTENTE");
        demande.setDateDemande(LocalDate.now());

        return demandeRepository.save(demande);
    }

    @Override
    public List<DemandeFormateur> getDemandesEnAttente() {
        return demandeRepository.findByStatut("EN_ATTENTE");
    }

    @Override
    public DemandeFormateur approuverDemande(Long demandeId) {
        DemandeFormateur demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));

        demande.setStatut("APPROUVEE");

        // Mise à jour du rôle de l'utilisateur
        User user = demande.getUser();
        user.setRoleUser(RoleUser.FORMATEUR);
        userRepository.save(user);

        return demandeRepository.save(demande);
    }

    @Override
    public DemandeFormateur rejeterDemande(Long demandeId) {
        DemandeFormateur demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));

        demande.setStatut("REJETEE");
        return demandeRepository.save(demande);
    }

    @Override
    public List<DemandeFormateur> getAllDemandes() {
        return demandeRepository.findAll();
    }

    // Injection nécessaire pour approuverDemande
    private UserRepository userRepository() {
        return userRepository;
    }
}