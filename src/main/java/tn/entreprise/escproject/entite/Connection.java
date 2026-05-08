package tn.entreprise.escproject.entite;

import jakarta.persistence.*;
import lombok.*;
import tn.entreprise.escproject.entite.enums.ConnectionStatus;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Connection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


     /* User qui envoie la demande */
    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

     /* User qui reçoit la demande     */
    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @Enumerated(EnumType.STRING)
    private ConnectionStatus status;

    private LocalDateTime createdAt;
}
