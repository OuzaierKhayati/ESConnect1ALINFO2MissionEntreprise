//package tn.entreprise.escproject.services;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import tn.entreprise.escproject.dto.MessageRequestDTO;
//import tn.entreprise.escproject.entite.Message;
//import tn.entreprise.escproject.entite.User;
//import tn.entreprise.escproject.repositories.MessageRepository;
//import tn.entreprise.escproject.repositories.UserRepository;
//import tn.entreprise.escproject.services.Interfaces.IMessageService;
//import java.time.LocalDateTime;
//import java.util.List;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import tn.entreprise.escproject.dto.NotificationDTO;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//
//@Service
//@RequiredArgsConstructor
//public class MessageServiceImp implements IMessageService {
//
//    private final MessageRepository messageRepository;
//    private final UserRepository userRepository;
//    private final SimpMessagingTemplate messagingTemplate;
//
//    @Override
//    public Message sendMessage(MessageRequestDTO dto) {
//
//        User sender = userRepository.findById(dto.getSenderId())
//                .orElseThrow(() ->
//                        new RuntimeException("Sender not found"));
//
//        User receiver = userRepository.findById(dto.getReceiverId())
//                .orElseThrow(() ->
//                        new RuntimeException("Receiver not found"));
//
//        Message message = Message.builder()
//                .sender(sender)
//                .receiver(receiver)
//                .content(dto.getContent())
//                .sentAt(LocalDateTime.now())
//                .isRead(false)
//                .build();
//
//        Message savedMessage =
//                messageRepository.save(message);
//
////        messagingTemplate.convertAndSendToUser(
////                //receiver.getId().toString() signifie : envoyer au receiver spécifique
////                receiver.getId().toString(),
////                "/queue/messages", //queue/messages : canal privé
////                savedMessage
////        );
//
//        MessageResponseDTO response =
//                mapToDTO(savedMessage);
//        messagingTemplate.convertAndSend(
//                "/topic/messages/" + receiver.getId(),
//                response
//        );
//        return response;
//
//        NotificationDTO notification =
//                new NotificationDTO(
//
//                        "New message received",
//
//                        sender.getFirstName()
//                );
//
//        messagingTemplate.convertAndSend(
//
//                "/topic/notifications/" + receiver.getId(),
//
//                notification
//        );
//
//        return mapToDTO(savedMessage);
//    }
//
//    //sans pagination.
////    @Override
////    public List<Message> getConversation(Long user1, Long user2) {
////
////        return messageRepository.getConversation(user1, user2);
////    }
//
//    //avec pagination
//    @Override
//    public Page<Message> getConversation(
//            Long user1,
//            Long user2,
//            int page,
//            int size) {
//
//        return messageRepository.getConversation(
//
//                user1,
//                user2,
//
//                PageRequest.of(page, size)
//        );
//    }
//
//    @Override
//    public void deleteMessage(Long id) {
//
//        messageRepository.deleteById(id);
//    }
//
//    @Override
//    public Message markAsRead(Long id) {
//
//        Message message =
//                messageRepository.findById(id)
//
//                        .orElseThrow(() ->
//                                new RuntimeException(
//                                        "Message not found"
//                                ));
//
//        message.setRead(true);
//
//        return messageRepository.save(message);
//    }
//
//    @Override
//    public Message updateMessage(
//            Message message) {
//
//        return messageRepository.save(message);
//    }
//
//    private MessageResponseDTO mapToDTO(Message message) {
//
//        return MessageResponseDTO.builder()
//
//                .id(message.getId())
//
//                .senderId(
//                        message.getSender().getId()
//                )
//
//                .senderName(
//                        message.getSender().getFirstName()
//                )
//
//                .receiverId(
//                        message.getReceiver().getId()
//                )
//
//                .receiverName(
//                        message.getReceiver().getFirstName()
//                )
//
//                .content(message.getContent())
//
//                .fileUrl(message.getFileUrl())
//
//                .fileType(message.getFileType())
//
//                .isRead(message.isRead())
//
//                .sentAt(message.getSentAt())
//
//                .build();
//    }
//}

package tn.entreprise.escproject.services;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import org.springframework.stereotype.Service;

import tn.entreprise.escproject.dto.MessageRequestDTO;
import tn.entreprise.escproject.dto.MessageResponseDTO;
import tn.entreprise.escproject.dto.NotificationDTO;

import tn.entreprise.escproject.entite.Message;
import tn.entreprise.escproject.entite.User;

import tn.entreprise.escproject.repositories.MessageRepository;
import tn.entreprise.escproject.repositories.UserRepository;

import tn.entreprise.escproject.services.Interfaces.IMessageService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MessageServiceImp implements IMessageService {

    private final MessageRepository messageRepository;

    private final UserRepository userRepository;

    private final SimpMessagingTemplate messagingTemplate;

    // =========================================================
    // CREATE MESSAGE ENTITY
    // =========================================================

    @Override
    public Message createMessageEntity(
            MessageRequestDTO dto) {

        User sender = userRepository.findById(dto.getSenderId())
                .orElseThrow(() ->
                        new RuntimeException("Sender not found"));

        User receiver = userRepository.findById(dto.getReceiverId())
                .orElseThrow(() ->
                        new RuntimeException("Receiver not found"));

        Message message = Message.builder()

                .sender(sender)

                .receiver(receiver)

                .content(dto.getContent())

                .sentAt(LocalDateTime.now())

                .isRead(false)

                .build();

        return messageRepository.save(message);
    }

    // =========================================================
    // SEND MESSAGE
    // =========================================================

    @Override
    public MessageResponseDTO sendMessage(
            MessageRequestDTO dto) {

        Message savedMessage =
                createMessageEntity(dto);

        MessageResponseDTO response =
                mapToDTO(savedMessage);

        // =====================================================
        // PRIVATE MESSAGE
        // =====================================================

        messagingTemplate.convertAndSend(

                "/topic/messages/"
                        + savedMessage.getReceiver().getId(),

                response
        );

        // =====================================================
        // NOTIFICATION
        // =====================================================

        NotificationDTO notification = NotificationDTO.builder()
                .message("New message received from " + savedMessage.getSender().getFirstName())
                .type("MESSAGE")
                .build();

        messagingTemplate.convertAndSend(

                "/topic/notifications/"
                        + savedMessage.getReceiver().getId(),

                notification
        );

        return response;
    }

    // =========================================================
    // GET CONVERSATION WITH PAGINATION
    // =========================================================

    @Override
    public Page<MessageResponseDTO> getConversation(

            Long user1,

            Long user2,

            int page,

            int size) {

        return messageRepository.getConversation(

                        user1,
                        user2,

                        PageRequest.of(page, size)
                )

                .map(this::mapToDTO);
    }

    // =========================================================
    // DELETE MESSAGE
    // =========================================================

    @Override
    public void deleteMessage(Long id) {

        messageRepository.deleteById(id);
    }

    // =========================================================
    // MARK AS READ
    // =========================================================

    @Override
    public MessageResponseDTO markAsRead(Long id) {

        Message message =
                messageRepository.findById(id)

                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Message not found"
                                ));

        message.setRead(true);

        Message updatedMessage =
                messageRepository.save(message);

        return mapToDTO(updatedMessage);
    }

    // =========================================================
    // UPDATE MESSAGE
    // =========================================================

    @Override
    public Message updateMessage(
            Message message) {

        return messageRepository.save(message);
    }

    // =========================================================
    // DTO MAPPING
    // =========================================================

    private MessageResponseDTO mapToDTO(
            Message message) {

        return MessageResponseDTO.builder()

                .id(message.getId())

                .senderId(
                        message.getSender().getId()
                )

                .senderName(
                        message.getSender().getFirstName()
                )

                .receiverId(
                        message.getReceiver().getId()
                )

                .receiverName(
                        message.getReceiver().getFirstName()
                )

                .content(message.getContent())

                .fileUrl(message.getFileUrl())

                .fileType(message.getFileType())

                .isRead(message.isRead())

                .sentAt(message.getSentAt())

                .build();
    }
}