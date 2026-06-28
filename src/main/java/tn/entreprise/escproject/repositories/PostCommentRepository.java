package tn.entreprise.escproject.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import tn.entreprise.escproject.entite.Post;
import tn.entreprise.escproject.entite.PostComment;

import java.util.List;

@Repository
public interface PostCommentRepository extends CrudRepository<PostComment, Long> {
    List<PostComment> findByPostOrderByCreatedAtDesc(Post post);
}
