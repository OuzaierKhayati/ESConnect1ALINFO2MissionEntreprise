package tn.entreprise.escproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageRequestDTO {

    @NotNull
    private Long senderId;

    private Long receiverId;

    private Long groupId;

    @NotBlank
    private String content;

    private boolean forceSend;
}