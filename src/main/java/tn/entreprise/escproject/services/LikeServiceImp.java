package tn.entreprise.escproject.services;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import tn.entreprise.escproject.dto.LikeResponse;
import tn.entreprise.escproject.entite.Like;
import tn.entreprise.escproject.entite.Publication;
import tn.entreprise.escproject.entite.User;
import tn.entreprise.escproject.exception.ResourceNotFoundException;
import tn.entreprise.escproject.repositories.LikeRepository;
import tn.entreprise.escproject.repositories.PublicationRepository;
import tn.entreprise.escproject.repositories.UserRepository;
import tn.entreprise.escproject.services.Interfaces.ILikeService;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeServiceImp implements ILikeService {

    private final LikeRepository likeRepository;
    private final PublicationRepository publicationRepository;
    private final UserRepository userRepository;

    @Override
    public LikeResponse toggleLike(Long publicationId, Long userId) {
        Publication publication = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Publication non trouvee avec l'ID: " + publicationId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve avec l'ID: " + userId));

        Optional<Like> existingLike = likeRepository.findByUserAndPublication(user, publication);

        boolean isLiked;
        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            isLiked = false;
        } else {
            Like like = Like.builder()
                    .user(user)
                    .publication(publication)
                    .build();
            likeRepository.save(like);
            isLiked = true;
        }

        long totalLikes = likeRepository.countByPublication(publication);

        return LikeResponse.builder()
                .liked(isLiked)
                .totalLikes(totalLikes)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserLiked(Long publicationId, Long userId) {
        Publication publication = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Publication non trouvee"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve"));

        return likeRepository.existsByUserAndPublication(user, publication);
    }

    @Override
    @Transactional(readOnly = true)
    public long countLikes(Long publicationId) {
        Publication publication = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Publication non trouvee"));
        return likeRepository.countByPublication(publication);
    }
}