package tn.entreprise.escproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupResponseDTO {

    private Long id;

    private String name;

    private Long creatorId;

    private LocalDateTime createdAt;

    private List<Long> memberIds;
}