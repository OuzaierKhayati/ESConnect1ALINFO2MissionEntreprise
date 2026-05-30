package tn.entreprise.escproject.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import tn.entreprise.escproject.dto.CommentaireDTO;
import tn.entreprise.escproject.dto.CommentaireRequest;
import tn.entreprise.escproject.entite.Commentaire;
import tn.entreprise.escproject.entite.Publication;
import tn.entreprise.escproject.entite.RoleUser;
import tn.entreprise.escproject.entite.User;
import tn.entreprise.escproject.exception.ResourceNotFoundException;
import tn.entreprise.escproject.exception.UnauthorizedException;
import tn.entreprise.escproject.repositories.CommentaireRepository;
import tn.entreprise.escproject.repositories.PublicationRepository;
import tn.entreprise.escproject.repositories.UserRepository;
import tn.entreprise.escproject.services.Interfaces.ICommentaireService;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentaireServiceImp implements ICommentaireService {

    private final CommentaireRepository commentaireRepository;
    private final PublicationRepository publicationRepository;
    private final UserRepository userRepository;

    @Override
    public CommentaireDTO ajouterCommentaire(CommentaireRequest request) {
        Publication publication = publicationRepository.findById(request.getPublicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Publication non trouvee avec l'ID: " + request.getPublicationId()));

        User auteur = userRepository.findById(request.getAuteurId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve avec l'ID: " + request.getAuteurId()));

        Commentaire commentaire = Commentaire.builder()
                .contenu(request.getContenu())
                .publication(publication)
                .auteur(auteur)
                .build();

        Commentaire savedCommentaire = commentaireRepository.save(commentaire);
        return convertToDTO(savedCommentaire);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentaireDTO> getCommentairesByPublication(Long publicationId) {
        Publication publication = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Publication non trouvee avec l'ID: " + publicationId));

        List<Commentaire> commentaires = commentaireRepository.findByPublicationOrderByDateCreationAsc(publication);

        return commentaires.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentaireDTO> getCommentairesPaginated(Long publicationId, int page, int size) {
        Publication publication = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Publication non trouvee avec l'ID: " + publicationId));

        Pageable pageable = PageRequest.of(page, size);
        Page<Commentaire> commentaires = commentaireRepository.findByPublicationOrderByDateCreationDesc(publication, pageable);

        return commentaires.map(this::convertToDTO);
    }

    @Override
    public CommentaireDTO updateCommentaire(Long commentaireId, String contenu, Long userId) {
        Commentaire commentaire = commentaireRepository.findById(commentaireId)
                .orElseThrow(() -> new ResourceNotFoundException("Commentaire non trouve avec l'ID: " + commentaireId));

        if (!commentaire.getAuteur().getId().equals(userId)) {
            throw new UnauthorizedException("Vous n'etes pas autorise a modifier ce commentaire");
        }

        commentaire.setContenu(contenu);
        Commentaire updatedCommentaire = commentaireRepository.save(commentaire);
        return convertToDTO(updatedCommentaire);
    }

    @Override
    public void deleteCommentaire(Long commentaireId, Long userId) {
        Commentaire commentaire = commentaireRepository.findById(commentaireId)
                .orElseThrow(() -> new ResourceNotFoundException("Commentaire non trouve avec l'ID: " + commentaireId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve"));

        boolean isCommentAuthor = commentaire.getAuteur().getId().equals(userId);
        boolean isPublicationAuthor = commentaire.getPublication().getAuteur().getId().equals(userId);
        boolean isAdmin = user.getRoleUser() == RoleUser.ADMIN;

        if (!isCommentAuthor && !isPublicationAuthor && !isAdmin) {
            throw new UnauthorizedException("Vous n'etes pas autorise a supprimer ce commentaire");
        }

        commentaireRepository.delete(commentaire);
    }

    private CommentaireDTO convertToDTO(Commentaire commentaire) {
        User auteur = commentaire.getAuteur();

        return CommentaireDTO.builder()
                .id(commentaire.getId())
                .contenu(commentaire.getContenu())
                .dateCreation(commentaire.getDateCreation())
                .dateModification(commentaire.getDateModification())
                .auteurId(auteur.getId())
                .auteurFirstName(auteur.getFirstName())
                .auteurLastName(auteur.getLastName())
                .publicationId(commentaire.getPublication().getId())
                .build();
    }
}