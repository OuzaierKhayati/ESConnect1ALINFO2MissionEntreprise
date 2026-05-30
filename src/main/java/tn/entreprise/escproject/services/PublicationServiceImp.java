package tn.entreprise.escproject.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import tn.entreprise.escproject.dto.PublicationDTO;
import tn.entreprise.escproject.dto.PublicationRequest;
import tn.entreprise.escproject.entite.Publication;
import tn.entreprise.escproject.entite.RoleUser;
import tn.entreprise.escproject.entite.User;
import tn.entreprise.escproject.exception.BadRequestException;
import tn.entreprise.escproject.exception.ResourceNotFoundException;
import tn.entreprise.escproject.exception.UnauthorizedException;
import tn.entreprise.escproject.repositories.LikeRepository;
import tn.entreprise.escproject.repositories.PublicationRepository;
import tn.entreprise.escproject.repositories.UserRepository;
import tn.entreprise.escproject.services.Interfaces.IPublicationService;

@Service
@RequiredArgsConstructor
@Transactional
public class PublicationServiceImp implements IPublicationService {

    private final PublicationRepository publicationRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;

    @Override
    public PublicationDTO creerPublication(PublicationRequest request) {
        User auteur = userRepository.findById(request.getAuteurId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve avec l'ID: " + request.getAuteurId()));

        Publication publication = Publication.builder()
                .contenu(request.getContenu())
                .imageUrl(request.getImageUrl())
                .auteur(auteur)
                .build();

        Publication savedPublication = publicationRepository.save(publication);
        return convertToDTO(savedPublication, request.getAuteurId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PublicationDTO> getFeed(int page, int size, Long currentUserId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Publication> publications = publicationRepository.findAllByOrderByDateCreationDesc(pageable);
        return publications.map(pub -> convertToDTO(pub, currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public PublicationDTO getPublicationById(Long id, Long currentUserId) {
        Publication publication = publicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publication non trouvee avec l'ID: " + id));
        return convertToDTO(publication, currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicationDTO> getPublicationsByUser(Long userId, Long currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve avec l'ID: " + userId));

        List<Publication> publications = publicationRepository.findByAuteurOrderByDateCreationDesc(user);

        return publications.stream()
                .map(pub -> convertToDTO(pub, currentUserId))
                .collect(Collectors.toList());
    }

    @Override
    public PublicationDTO updatePublication(Long id, PublicationRequest request, Long userId) {
        Publication publication = publicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publication non trouvee avec l'ID: " + id));

        if (!publication.getAuteur().getId().equals(userId)) {
            throw new UnauthorizedException("Vous n'etes pas autorise a modifier cette publication");
        }

        publication.setContenu(request.getContenu());
        if (request.getImageUrl() != null) {
            publication.setImageUrl(request.getImageUrl());
        }

        Publication updatedPublication = publicationRepository.save(publication);
        return convertToDTO(updatedPublication, userId);
    }

    @Override
    public void deletePublication(Long id, Long userId) {
        Publication publication = publicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publication non trouvee avec l'ID: " + id));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve"));

        boolean isAuteur = publication.getAuteur().getId().equals(userId);
        boolean isAdmin = user.getRoleUser() == RoleUser.ADMIN;

        if (!isAuteur && !isAdmin) {
            throw new UnauthorizedException("Vous n'etes pas autorise a supprimer cette publication");
        }

        publicationRepository.delete(publication);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PublicationDTO> searchPublications(String keyword, int page, int size, Long currentUserId) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new BadRequestException("Le mot-cle de recherche ne peut pas etre vide");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Publication> publications = publicationRepository.searchByContenu(keyword.trim(), pageable);
        return publications.map(pub -> convertToDTO(pub, currentUserId));
    }

    private PublicationDTO convertToDTO(Publication publication, Long currentUserId) {
        User auteur = publication.getAuteur();

        boolean likedByCurrentUser = false;
        if (currentUserId != null) {
            User currentUser = userRepository.findById(currentUserId).orElse(null);
            if (currentUser != null) {
                likedByCurrentUser = likeRepository.existsByUserAndPublication(currentUser, publication);
            }
        }

        return PublicationDTO.builder()
                .id(publication.getId())
                .contenu(publication.getContenu())
                .imageUrl(publication.getImageUrl())
                .dateCreation(publication.getDateCreation())
                .dateModification(publication.getDateModification())
                .auteurId(auteur.getId())
                .auteurFirstName(auteur.getFirstName())
                .auteurLastName(auteur.getLastName())
                .auteurEmail(auteur.getEmail())
                .nombreLikes(publication.getNombreLikes())
                .nombreCommentaires(publication.getNombreCommentaires())
                .likedByCurrentUser(likedByCurrentUser)
                .build();
    }
}