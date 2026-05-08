package tn.entreprise.escproject.entite;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
//@Builder
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

     /* Expéditeur */
    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    /* Destinataire */
    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @Column(columnDefinition = "TEXT") /* TEXT : car les messages peuvent être longs.*/
    private String content;

    private LocalDateTime sentAt;

    private boolean isRead;
}
