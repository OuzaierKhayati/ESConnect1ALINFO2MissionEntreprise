package tn.entreprise.escproject.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.entreprise.escproject.entite.Post;
import tn.entreprise.escproject.entite.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
public interface PostRepository extends CrudRepository<Post, Long> {
    List<Post> findAllByOrderByCreatedAtDesc();
    List<Post> findByAuthorOrderByCreatedAtDesc(User author);
    List<Post> findByOriginalPostOrderByCreatedAtDesc(Post originalPost);
    long countByOriginalPost(Post originalPost);
    boolean existsByOriginalPostIdAndAuthorId(Long originalPostId, Long authorId);

    @Query("SELECT p.originalPost.id, COUNT(p) FROM Post p WHERE p.originalPost IS NOT NULL GROUP BY p.originalPost.id")
    List<Object[]> countSharesByPost();

    @Query("SELECT DISTINCT p.author.id FROM Post p JOIN p.likedBy u WHERE u.id = :userId AND p.createdAt >= :since")
    List<Long> findAuthorIdsLikedByUser(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT DISTINCT p.originalPost.id FROM Post p WHERE p.author.id = :userId AND p.originalPost IS NOT NULL")
    Set<Long> findOriginalPostIdsSharedByUser(@Param("userId") Long userId);
}
