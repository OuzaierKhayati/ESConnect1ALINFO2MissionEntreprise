package tn.entreprise.escproject.dto;

import lombok.Data;

@Data
public class SmartRepliesRequestDTO {

    private String lastMessage;
    private String conversationContext;
}
