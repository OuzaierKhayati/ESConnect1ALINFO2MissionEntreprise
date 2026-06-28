package tn.entreprise.escproject.entite;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 3000)
    private String content;

    @ElementCollection
    @CollectionTable(name = "post_media_urls", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "media_url", length = 500)
    private List<String> mediaUrls = new ArrayList<>();

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "author_id")
    @JsonIgnore
    private User author;

    @ManyToMany
    @JoinTable(
        name = "post_likes",
        joinColumns = @JoinColumn(name = "post_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private List<User> likedBy = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<PostComment> comments = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "original_post_id")
    @JsonIgnore
    private Post originalPost;

    @Column(length = 1000)
    private String shareText;
}
