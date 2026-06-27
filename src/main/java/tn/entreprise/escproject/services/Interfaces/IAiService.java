package tn.entreprise.escproject.services.Interfaces;

import tn.entreprise.escproject.dto.MessageResponseDTO;
import java.util.List;

public interface IAiService {

    /** Returns toxicity score 0.0–1.0. Returns -1 on API failure (fail-open). */
    double checkToxicity(String text);

    /** Summarizes a list of messages into a short paragraph. */
    String summarizeConversation(List<MessageResponseDTO> messages);

    /** Returns 3 short smart-reply suggestions for the given message. */
    List<String> getSmartReplies(String lastMessage, String context);
}
