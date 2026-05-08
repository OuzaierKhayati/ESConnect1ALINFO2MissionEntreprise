package tn.entreprise.escproject.dto;

import lombok.Data;

@Data
public class ConnectionRequestDTO {

    private Long senderId;

    private Long receiverId;
}