package tn.entreprise.escproject.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import tn.entreprise.escproject.dto.MessageResponseDTO;
import tn.entreprise.escproject.services.Interfaces.IAiService;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceImpl implements IAiService {

    @Value("${ai.groq.api-key}")
    private String groqApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String GROQ_MODEL = "llama-3.1-8b-instant";

    // =========================================================
    // LOCAL KEYWORD-BASED TOXICITY FILTER
    // Returns 1.0 if offensive content detected, 0.0 otherwise.
    // =========================================================

    private static final Set<String> BLOCKLIST = new HashSet<>(Arrays.asList(
            //
            //-------------------------------
            // To anyone reading this, we sincerely apologize for the inappropriate words in the code.
            //--------------------------------
            //
        // English
        "fuck", "f**k", "fck", "fucker", "fucking", "fucked", "fuk", "fvck",
        "shit", "sh*t", "shitty", "bullshit",
        "asshole", "ass", "asses",
        "bitch", "b*tch", "bitches",
        "bastard",
        "dick", "cock", "pussy",
        "cunt",
        "idiot", "stupid", "moron", "retard", "dumbass",
        "kill yourself", "kys", "go die",
        "hate you", "i hate you",
        "nigger", "nigga",
        "faggot", "fag",
        "slut", "whore",
        "damn", "damnit",
        // French
        "merde", "merde!", "putain", "salope", "enculé", "encule", "connard",
        "connasse", "batard", "bâtard", "fils de pute", "va te faire foutre",
        "va te faire", "ferme ta gueule", "ta gueule", "nique", "niquer",
        "pute", "fdp", "tg",
        // Arabic transliteration (common in Tunisia)
        "kess", "zebi", "zbou", "kalb", "kelb", "nik", "manyak",
        "sharmouta", "7mar", "7aywan", "barra", "kahba"
    ));

    // Matches whole words only (avoids flagging "classic" for "ass")
    private static final Pattern WORD_SPLITTER = Pattern.compile("[\\s.,!?;:\"'()\\[\\]{}\\-_/\\\\]+");

    @Override
    public double checkToxicity(String text) {
        if (text == null || text.isBlank()) {
            return 0.0;
        }

        String normalized = text.toLowerCase().trim();

        // 1. Check for exact blocked phrases (multi-word)
        for (String term : BLOCKLIST) {
            if (term.contains(" ") && normalized.contains(term)) {
                log.info("Toxic phrase detected: [{}]", term);
                return 1.0;
            }
        }

        // 2. Check individual words
        String[] words = WORD_SPLITTER.split(normalized);
        for (String word : words) {
            if (!word.isEmpty() && BLOCKLIST.contains(word)) {
                log.info("Toxic word detected: [{}]", word);
                return 1.0;
            }
        }

        return 0.0;
    }

    // =========================================================
    // CONVERSATION SUMMARY (Gemini API)
    // =========================================================

    @Override
    public String summarizeConversation(List<MessageResponseDTO> messages) {
        if (messages == null || messages.isEmpty()) {
            return "No messages to summarize.";
        }

        String conversationText = messages.stream()
            .filter(m -> m.getContent() != null && !m.getContent().isBlank())
            .map(m -> m.getSenderName() + ": " + m.getContent())
            .collect(Collectors.joining("\n"));

        if (conversationText.isBlank()) {
            return "No text messages found to summarize.";
        }

        String prompt = """
            Summarize the following conversation in 3-5 clear sentences. \
            Focus on the main topics discussed and any conclusions reached. \
            Be concise and neutral. Write in the same language as the conversation.

            Conversation:
            """ + conversationText;

        return callAi(prompt);
    }

    // =========================================================
    // SMART REPLIES (Groq API)
    // =========================================================

    @Override
    public List<String> getSmartReplies(String lastMessage, String context) {
        if (lastMessage == null || lastMessage.isBlank()) {
            return List.of("Ok!", "Sure", "Got it");
        }

        String prompt = """
            Given this message: "%s"
            Generate exactly 3 very short, natural reply suggestions (max 8 words each).
            Return ONLY a JSON array of strings, nothing else, no markdown.
            Example: ["Sure, sounds good!", "Let me check", "I'll get back to you"]
            """.formatted(lastMessage);

        String raw = callAi(prompt);
        return parseSmartReplies(raw);
    }

    // =========================================================
    // PRIVATE HELPERS
    // =========================================================

    private String callAi(String prompt) {
        try {
            Map<String, Object> message = Map.of("role", "user", "content", prompt);
            Map<String, Object> body = Map.of(
                "model", GROQ_MODEL,
                "messages", List.of(message),
                "max_tokens", 512,
                "temperature", 0.3
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqApiKey);

            ResponseEntity<String> response = restTemplate.exchange(
                GROQ_URL,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            String text = root
                .path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText(null);

            if (text == null || text.isBlank()) {
                log.warn("Groq returned empty response. Full body: {}", response.getBody());
                return "Unable to generate response.";
            }
            return text;

        } catch (HttpClientErrorException e) {
            log.error("Groq API HTTP error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return "AI service unavailable (check API key).";
        } catch (Exception e) {
            log.error("Groq API call failed: {}", e.getMessage());
            return "Unable to generate response.";
        }
    }

    private List<String> parseSmartReplies(String raw) {
        if (raw == null || raw.startsWith("AI service") || raw.startsWith("Unable")) {
            return List.of("Sure!", "Sounds good", "I'll check");
        }
        try {
            int start = raw.indexOf('[');
            int end = raw.lastIndexOf(']');
            if (start >= 0 && end > start) {
                String json = raw.substring(start, end + 1);
                JsonNode node = objectMapper.readTree(json);
                List<String> replies = new ArrayList<>();
                node.forEach(n -> replies.add(n.asText()));
                if (!replies.isEmpty()) return replies;
            }
        } catch (Exception e) {
            log.warn("Failed to parse smart replies JSON: {}", e.getMessage());
        }
        return List.of("Sure!", "Sounds good", "I'll check");
    }
}
