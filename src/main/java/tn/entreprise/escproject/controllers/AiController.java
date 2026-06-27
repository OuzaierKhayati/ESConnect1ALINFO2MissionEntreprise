package tn.entreprise.escproject.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import tn.entreprise.escproject.dto.*;
import tn.entreprise.escproject.services.Interfaces.IAiService;
import tn.entreprise.escproject.services.Interfaces.IMessageService;

import java.util.List;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final IAiService aiService;
    private final IMessageService messageService;

    // =========================================================
    // CONVERSATION SUMMARY
    // =========================================================

    @PostMapping("/summarize")
    public AiSummarizeResponseDTO summarize(@RequestBody AiSummarizeRequestDTO request) {
        List<MessageResponseDTO> messages;

        if (request.getGroupId() != null) {
            messages = messageService.getGroupConversation(request.getGroupId());
        } else {
            Page<MessageResponseDTO> page = messageService.getConversation(
                    request.getUserId1(), request.getUserId2(), 0, 40);
            messages = page.getContent();
        }

        String summary = aiService.summarizeConversation(messages);
        return AiSummarizeResponseDTO.builder().summary(summary).build();
    }

    // =========================================================
    // SMART REPLIES
    // =========================================================

    @PostMapping("/smart-replies")
    public SmartRepliesResponseDTO smartReplies(@RequestBody SmartRepliesRequestDTO request) {
        List<String> replies = aiService.getSmartReplies(
                request.getLastMessage(),
                request.getConversationContext()
        );
        return SmartRepliesResponseDTO.builder().replies(replies).build();
    }
}
