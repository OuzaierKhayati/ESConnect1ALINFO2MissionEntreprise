package tn.entreprise.escproject.dto;

import lombok.Builder;
import lombok.Data;
import tn.entreprise.escproject.entite.enums.ConnectionStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class ConnectionResponseDTO {

    private Long id;

    private ConnectionUserDTO sender;

    private ConnectionUserDTO receiver;

    private ConnectionStatus status;

    private LocalDateTime createdAt;
}
