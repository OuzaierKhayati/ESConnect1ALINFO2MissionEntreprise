package tn.entreprise.escproject.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import tn.entreprise.escproject.services.Interfaces.IMessageService;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    private final IMessageService messageService;

    @MessageMapping("/sendMessage")
    public void sendMessage(Map<String, Object> message) {
        broadcastMessage(message);
    }

    @MessageMapping("/groupMessage")
    public void groupMessage(Map<String, Object> message) {
        broadcastMessage(message);
    }

    @MessageMapping("/reactMessage")
    public void reactMessage(Map<String, Object> payload) {
        broadcastMessage(payload);
    }

    private void broadcastMessage(Map<String, Object> message) {
        Long senderId = toLong(message.get("senderId"));
        Long receiverId = toLong(message.get("receiverId"));
        Long groupId = toLong(message.get("groupId"));

        if (groupId != null && groupId > 0) {
            List<Long> memberIds = messageService.getGroupMemberIds(groupId);
            for (Long memberId : memberIds) {
                if (senderId != null && senderId.equals(memberId)) {
                    continue;
                }

                messagingTemplate.convertAndSend("/topic/groups/" + memberId, (Object) message);
            }
            return;
        }

        if (receiverId == null || receiverId <= 0) {
            return;
        }

        messagingTemplate.convertAndSend("/topic/messages/" + receiverId, (Object) message);
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}