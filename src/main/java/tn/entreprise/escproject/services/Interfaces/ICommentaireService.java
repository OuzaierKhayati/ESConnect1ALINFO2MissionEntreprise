package tn.entreprise.escproject.services.Interfaces;

import java.util.List;

import org.springframework.data.domain.Page;

import tn.entreprise.escproject.dto.CommentaireDTO;
import tn.entreprise.escproject.dto.CommentaireRequest;

public interface ICommentaireService {

    CommentaireDTO ajouterCommentaire(CommentaireRequest request);

    List<CommentaireDTO> getCommentairesByPublication(Long publicationId);

    Page<CommentaireDTO> getCommentairesPaginated(Long publicationId, int page, int size);

    CommentaireDTO updateCommentaire(Long commentaireId, String contenu, Long userId);

    void deleteCommentaire(Long commentaireId, Long userId);
}