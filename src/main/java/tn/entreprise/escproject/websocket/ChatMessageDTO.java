package tn.entreprise.escproject.websocket;

import lombok.Data;

@Data
public class ChatMessageDTO {

    private String sender;

    private String receiver;

    private String content;
}