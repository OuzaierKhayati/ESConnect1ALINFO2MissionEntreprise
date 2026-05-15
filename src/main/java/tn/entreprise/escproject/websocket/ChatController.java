package tn.entreprise.escproject.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    @MessageMapping("/sendMessage")

    @SendTo("/topic/messages")
    public ChatMessageDTO sendMessage(
            ChatMessageDTO message) {

        return message;
    }
}