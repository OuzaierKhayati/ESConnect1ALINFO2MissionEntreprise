//package tn.entreprise.escproject.controllers;
//
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.*;
//import tn.entreprise.escproject.dto.MessageRequestDTO;
//import tn.entreprise.escproject.entite.Message;
//import tn.entreprise.escproject.services.Interfaces.IMessageService;
//import org.springframework.web.multipart.MultipartFile;
//import tn.entreprise.escproject.services.UploadService;
//
//import java.io.IOException;
//import java.util.List;
//
//@RestController
//@RequestMapping("/messages")
//@RequiredArgsConstructor
//public class MessageController {
//
//    private final IMessageService messageService;
//    private final UploadService uploadService;
//
//    @PostMapping
//    public MessageResponseDTO  sendMessage(
//            @Valid @RequestBody MessageRequestDTO dto) {
//
//        return messageService.sendMessage(dto);
//    }
//
//    //sans pagiation
////    @GetMapping("/conversation")
////    public List<Message> getConversation(
////            @RequestParam Long user1,
////            @RequestParam Long user2) {
////
////        return messageService.getConversation(user1, user2);
////    }
//
//    //avec pagination
//    @GetMapping("/conversation")
//    public Page<Message> getConversation(
//
//            @RequestParam Long user1,
//
//            @RequestParam Long user2,
//
//            @RequestParam(defaultValue = "0")
//            int page,
//
//            @RequestParam(defaultValue = "20")
//            int size) {
//
//        return messageService.getConversation(
//                user1,
//                user2,
//                page,
//                size
//        );
//    }
//
//    @DeleteMapping("/{id}")
//    public void deleteMessage(@PathVariable Long id) {
//
//        messageService.deleteMessage(id);
//    }
//
//    @PutMapping("/{id}/read")
//    public MessageResponseDTO  markAsRead(
//            @PathVariable Long id) {
//
//        return messageService.markAsRead(id);
//    }
//
//    /// // on n'a pas besoin de cette methode maintenat on : POST messages/with-file
////    // ceci ne donne pas la possibilité d'ajouter de joindre une fichier
////    //@PostMapping("/upload")
////
////    //solution
////    @PostMapping(
////            value = "/upload",
////            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
////    )
////    public String uploadFile(
////
////            @RequestParam("file")
////            MultipartFile file)
////
////            throws IOException {
////
////        return uploadService.uploadFile(file);
////    }
//
//    @PostMapping(
//            value = "/with-file",
//            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
//    )
//    public MessageResponseDTO sendMessageWithFile(
//
//            @RequestParam Long senderId,
//
//            @RequestParam Long receiverId,
//
//            @RequestParam(required = false)
//            String content,
//
//            @RequestParam("file")
//            MultipartFile file)
//
//            throws IOException {
//
//        // upload file
//        String fileUrl =
//                uploadService.uploadFile(file);
//
//        // create DTO
//        MessageRequestDTO dto =
//                new MessageRequestDTO();
//
//        dto.setSenderId(senderId);
//
//        dto.setReceiverId(receiverId);
//
//        dto.setContent(content);
//
//        // create message
//        Message message =
//                messageService.sendMessage(dto);
//
//        // add file data
//        message.setFileUrl(fileUrl);
//
//        message.setFileType(
//                file.getContentType()
//        );
//
//        return messageService.updateMessage(message);
//    }
//}

package tn.entreprise.escproject.controllers;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;

import org.springframework.http.MediaType;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import tn.entreprise.escproject.dto.GroupCreateRequest;
import tn.entreprise.escproject.dto.GroupResponseDTO;
import tn.entreprise.escproject.dto.MessageRequestDTO;
import tn.entreprise.escproject.dto.MessageResponseDTO;
import tn.entreprise.escproject.dto.MessageSendResponseDTO;

import tn.entreprise.escproject.entite.Message;

import tn.entreprise.escproject.services.Interfaces.IMessageService;
import tn.entreprise.escproject.services.UploadService;

import java.io.IOException;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final IMessageService messageService;

    private final UploadService uploadService;

    // =========================================================
    // SEND MESSAGE
    // =========================================================

    @PostMapping
    public MessageSendResponseDTO sendMessage(

            @Valid
            @RequestBody
            MessageRequestDTO dto) {

        return messageService.sendMessage(dto);
    }

    // =========================================================
    // GET CONVERSATION WITH PAGINATION
    // =========================================================

    @GetMapping("/conversation")
    public Page<MessageResponseDTO> getConversation(

            @RequestParam Long user1,

            @RequestParam Long user2,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size) {

        return messageService.getConversation(

                user1,
                user2,
                page,
                size
        );
    }

        @PostMapping("/groups")
        public GroupResponseDTO createGroup(
                        @Valid @RequestBody GroupCreateRequest request,
                        @RequestParam(required = false) Long userId) {

                if (request.getUserId() == null && userId != null) {
                        request.setUserId(userId);
                }

                return messageService.createGroup(request);
        }

        @GetMapping("/groups")
        public java.util.List<GroupResponseDTO> getGroups(
                        @RequestParam Long userId) {

                return messageService.getGroupsForUser(userId);
        }

        @GetMapping("/groups/{groupId}/messages")
        public java.util.List<MessageResponseDTO> getGroupConversation(
                        @PathVariable Long groupId) {

                return messageService.getGroupConversation(groupId);
        }

        @PostMapping("/groups/{groupId}/members/{userId}")
        public GroupResponseDTO addMemberToGroup(
                        @PathVariable Long groupId,
                        @PathVariable Long userId) {

                return messageService.addMemberToGroup(groupId, userId);
        }

        @DeleteMapping("/groups/{groupId}/members/{userId}")
        public GroupResponseDTO removeMemberFromGroup(
                        @PathVariable Long groupId,
                        @PathVariable Long userId) {

                return messageService.removeMemberFromGroup(groupId, userId);
        }

        @DeleteMapping("/groups/{groupId}")
        public void deleteGroup(
                        @PathVariable Long groupId) {

                messageService.deleteGroup(groupId);
        }

    // =========================================================
    // DELETE MESSAGE
    // =========================================================

    @DeleteMapping("/{id}")
    public void deleteMessage(
            @PathVariable Long id) {

        messageService.deleteMessage(id);
    }

    // =========================================================
    // MARK MESSAGE AS READ
    // =========================================================

    @PutMapping("/{id}/read")
    public MessageResponseDTO markAsRead(

            @PathVariable Long id) {

        return messageService.markAsRead(id);
    }

    // =========================================================
    // SEND MESSAGE WITH FILE
    // =========================================================

    @PostMapping(

            value = "/with-file",

            consumes =
                    MediaType.MULTIPART_FORM_DATA_VALUE
    )

    public MessageResponseDTO sendMessageWithFile(

            @RequestParam Long senderId,

            @RequestParam(required = false) Long receiverId,

            @RequestParam(required = false) Long groupId,

            @RequestParam(required = false)
            String content,

            @RequestParam("file")
            MultipartFile file)

            throws IOException {

        // ==============================================
        // Upload file
        // ==============================================

        String fileUrl =
                uploadService.uploadFile(file);

        // ==============================================
        // Create DTO
        // ==============================================

        MessageRequestDTO dto =
                new MessageRequestDTO();

        dto.setSenderId(senderId);

        dto.setReceiverId(receiverId);

        dto.setGroupId(groupId);

        dto.setContent(content);

        // ==============================================
        // Create Message Entity
        // ==============================================

        Message message =
                messageService.createMessageEntity(dto);

        // ==============================================
        // Add File Data
        // ==============================================

        message.setFileUrl(fileUrl);

        message.setFileType(
                file.getContentType()
        );

        Message updatedMessage =
                messageService.updateMessage(message);

        return messageService.getMessageResponseById(updatedMessage.getId());
    }
}
