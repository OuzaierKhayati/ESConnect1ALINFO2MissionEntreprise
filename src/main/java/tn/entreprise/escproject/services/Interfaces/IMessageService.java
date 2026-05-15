//package tn.entreprise.escproject.services.Interfaces;
//
//import org.springframework.data.domain.Page;
//import tn.entreprise.escproject.dto.MessageRequestDTO;
//import tn.entreprise.escproject.entite.Message;
//
//import java.util.List;
//
//public interface IMessageService {
//
//    MessageResponseDTO sendMessage((MessageRequestDTO dto);
//
//    Page<Message> getConversation(Long user1, Long user2, int page, int size);
//
//    void deleteMessage(Long id);
//
//    MessageResponseDTO markAsRead(Long id);
//
//    Message updateMessage(Message message);
//}

package tn.entreprise.escproject.services.Interfaces;

import org.springframework.data.domain.Page;
import tn.entreprise.escproject.dto.MessageRequestDTO;
import tn.entreprise.escproject.dto.MessageResponseDTO;
import tn.entreprise.escproject.entite.Message;

public interface IMessageService {

    MessageResponseDTO sendMessage(MessageRequestDTO dto);

    Page<MessageResponseDTO> getConversation(Long user1, Long user2, int page, int size);

    void deleteMessage(Long id);

    MessageResponseDTO markAsRead(Long id);

    Message updateMessage(Message message);

    Message createMessageEntity(MessageRequestDTO dto);
}