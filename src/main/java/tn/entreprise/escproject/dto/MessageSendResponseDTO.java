package tn.entreprise.escproject.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageSendResponseDTO {

    /** "SENT" or "WARN" */
    private String status;

    /** Populated when status = SENT */
    private MessageResponseDTO data;

    /** Populated when status = WARN */
    private String warning;
}
