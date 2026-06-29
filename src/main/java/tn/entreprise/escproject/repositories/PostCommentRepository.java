package tn.entreprise.escproject.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.entreprise.escproject.entite.Post;
import tn.entreprise.escproject.entite.PostComment;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostCommentRepository extends CrudRepository<PostComment, Long> {
    List<PostComment> findByPostOrderByCreatedAtDesc(Post post);

    @Query("SELECT DISTINCT p.author.id FROM PostComment c JOIN c.post p WHERE c.user.id = :userId AND c.createdAt >= :since")
    List<Long> findAuthorIdsCommentedByUser(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}
