package tn.entreprise.escproject.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageResponseDTO {

    private Long id;

    private Long senderId;
    private String senderName;

    private Long receiverId;
    private String receiverName;

    private String content;

    private String fileUrl;

    private String fileType;

    private boolean isRead;

    private LocalDateTime sentAt;

    private Long groupId;

    private boolean flagged;
}