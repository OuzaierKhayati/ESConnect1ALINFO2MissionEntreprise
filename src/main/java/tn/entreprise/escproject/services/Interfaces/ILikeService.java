package tn.entreprise.escproject.services.Interfaces;

import tn.entreprise.escproject.dto.LikeResponse;

public interface ILikeService {

    LikeResponse toggleLike(Long publicationId, Long userId);

    boolean hasUserLiked(Long publicationId, Long userId);

    long countLikes(Long publicationId);
}