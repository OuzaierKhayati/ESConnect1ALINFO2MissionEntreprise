package tn.entreprise.escproject.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import tn.entreprise.escproject.entite.Post;
import tn.entreprise.escproject.entite.User;

import java.util.List;

@Repository
public interface PostRepository extends CrudRepository<Post, Long> {
    List<Post> findAllByOrderByCreatedAtDesc();
    List<Post> findByAuthorOrderByCreatedAtDesc(User author);
    List<Post> findByOriginalPostOrderByCreatedAtDesc(Post originalPost);
    long countByOriginalPost(Post originalPost);
    boolean existsByOriginalPostIdAndAuthorId(Long originalPostId, Long authorId);
}
