package tn.entreprise.escproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageRequestDTO {

    @NotNull
    private Long senderId;

    @NotNull
    private Long receiverId;

    @NotBlank
    private String content;
}