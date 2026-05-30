package tn.entreprise.escproject.services.Interfaces;

import java.util.List;

import org.springframework.data.domain.Page;

import tn.entreprise.escproject.dto.PublicationDTO;
import tn.entreprise.escproject.dto.PublicationRequest;

public interface IPublicationService {

    PublicationDTO creerPublication(PublicationRequest request);

    Page<PublicationDTO> getFeed(int page, int size, Long currentUserId);

    PublicationDTO getPublicationById(Long id, Long currentUserId);

    List<PublicationDTO> getPublicationsByUser(Long userId, Long currentUserId);

    PublicationDTO updatePublication(Long id, PublicationRequest request, Long userId);

    void deletePublication(Long id, Long userId);

    Page<PublicationDTO> searchPublications(String keyword, int page, int size, Long currentUserId);
}