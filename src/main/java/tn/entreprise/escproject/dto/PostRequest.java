package tn.entreprise.escproject.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostRequest {
    private String content;
    private List<String> mediaUrls;
    private Long originalPostId;
    private String shareText;
}
