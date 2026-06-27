package tn.entreprise.escproject.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmartRepliesResponseDTO {

    private List<String> replies;
}
