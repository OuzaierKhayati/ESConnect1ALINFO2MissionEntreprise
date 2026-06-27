package tn.entreprise.escproject.entite;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //FetchType.LAZY : pour éviter de charger les données de l'utilisateur à chaque fois que nous chargeons un message=> améliorer les performances.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "receiver_id", nullable = true)
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "group_id", nullable = true)
    private ChatGroup group;

    @Column(columnDefinition = "TEXT") /* TEXT : car les messages peuvent être longs.*/
    private String content;

    private LocalDateTime sentAt;

    private boolean isRead;

    private String fileUrl;

    private String fileType;

    private boolean flagged;
}
