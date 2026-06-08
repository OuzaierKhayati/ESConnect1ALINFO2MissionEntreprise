package tn.entreprise.escproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class GroupCreateRequest {

    @NotBlank
    private String name;

    @NotEmpty
    private List<Long> memberIds;

    private Long userId;

    private Long creatorId;
}