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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.entreprise.escproject.dto.GroupCreateRequest;
import tn.entreprise.escproject.dto.GroupResponseDTO;
import tn.entreprise.escproject.dto.MessageRequestDTO;
import tn.entreprise.escproject.dto.MessageResponseDTO;
import tn.entreprise.escproject.dto.MessageSendResponseDTO;
import tn.entreprise.escproject.dto.NotificationDTO;
import tn.entreprise.escproject.entite.ChatGroup;
import tn.entreprise.escproject.entite.Message;
import tn.entreprise.escproject.entite.User;
import tn.entreprise.escproject.entite.UserStatus;
import tn.entreprise.escproject.exception.BadRequestException;
import tn.entreprise.escproject.exception.ResourceNotFoundException;
import tn.entreprise.escproject.repositories.ChatGroupRepository;
import tn.entreprise.escproject.repositories.MessageRepository;
import tn.entreprise.escproject.repositories.UserRepository;
import tn.entreprise.escproject.services.Interfaces.IAiService;
import tn.entreprise.escproject.services.Interfaces.IMessageService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImp implements IMessageService {

    private final MessageRepository messageRepository;

    private final UserRepository userRepository;

    private final ChatGroupRepository chatGroupRepository;

    private final SimpMessagingTemplate messagingTemplate;

    private final IAiService aiService;

    private static final double TOXICITY_THRESHOLD = 0.70;
    private static final int MAX_WARNINGS = 3;

    @Override
    public Message createMessageEntity(MessageRequestDTO dto) {
        User sender = userRepository.findById(dto.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        ChatGroup group = null;
        User receiver = null;

        if (dto.getGroupId() != null) {
            group = chatGroupRepository.findById(dto.getGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + dto.getGroupId()));
        } else {
            if (dto.getReceiverId() == null || dto.getReceiverId() <= 0) {
                throw new BadRequestException("Receiver is required for direct messages");
            }

            receiver = userRepository.findById(dto.getReceiverId())
                    .orElseThrow(() -> new RuntimeException("Receiver not found"));
        }

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .group(group)
                .content(dto.getContent())
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .build();

        return messageRepository.save(message);
    }

    @Override
    public MessageSendResponseDTO sendMessage(MessageRequestDTO dto) {
        String content = dto.getContent();

        // ── Toxicity check (skip for file-only or force-send) ──────────────
        if (content != null && !content.isBlank() && !dto.isForceSend()) {
            double score = aiService.checkToxicity(content);
            if (score >= TOXICITY_THRESHOLD) {
                return MessageSendResponseDTO.builder()
                        .status("WARN")
                        .warning("Your message may contain offensive language. Edit anyway?")
                        .build();
            }
        }

        // ── Build and persist the message ───────────────────────────────────
        Message savedMessage = createMessageEntity(dto);

        if (dto.isForceSend()) {
            savedMessage.setFlagged(true);
            savedMessage = messageRepository.save(savedMessage);

            // Increment sender's warning counter
            User sender = savedMessage.getSender();
            sender.setWarningCount(sender.getWarningCount() + 1);
            log.info("User {} warning count: {}", sender.getId(), sender.getWarningCount());

            if (sender.getWarningCount() >= MAX_WARNINGS) {
                sender.setUserStatus(UserStatus.BLOCKED);
                log.warn("User {} has been BLOCKED after {} warnings", sender.getId(), MAX_WARNINGS);
            }
            userRepository.save(sender);
        }

        MessageResponseDTO response = mapToDTO(savedMessage);

        if (savedMessage.getGroup() != null) {
            broadcastGroupMessage(savedMessage, response);
        } else {
            broadcastDirectMessage(savedMessage, response);
        }

        return MessageSendResponseDTO.builder()
                .status("SENT")
                .data(response)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponseDTO> getConversation(Long user1, Long user2, int page, int size) {
        return messageRepository.getConversation(user1, user2, PageRequest.of(page, size))
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponseDTO> getGroupConversation(Long groupId) {
        return messageRepository.getGroupConversation(groupId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GroupResponseDTO createGroup(GroupCreateRequest request) {
        Long creatorId = request.getCreatorId() != null ? request.getCreatorId() : request.getUserId();
        if (creatorId == null || creatorId <= 0) {
            throw new BadRequestException("Creator is required to create a group");
        }

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + creatorId));

        Set<Long> uniqueMemberIds = new LinkedHashSet<>(request.getMemberIds());
        uniqueMemberIds.add(creatorId);

        List<User> members = new ArrayList<>();
        for (Long memberId : uniqueMemberIds) {
            User member = userRepository.findById(memberId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + memberId));
            members.add(member);
        }

        ChatGroup group = ChatGroup.builder()
                .name(request.getName().trim())
                .createdBy(creator)
                .members(members)
                .createdAt(LocalDateTime.now())
                .build();

        ChatGroup savedGroup = chatGroupRepository.save(group);
        return mapToGroupDTO(savedGroup);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupResponseDTO> getGroupsForUser(Long userId) {
        return chatGroupRepository.findGroupsForUser(userId)
                .stream()
                .map(this::mapToGroupDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getGroupMemberIds(Long groupId) {
        ChatGroup group = chatGroupRepository.findWithMembersById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        return group.getMembers().stream()
                .map(User::getId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GroupResponseDTO addMemberToGroup(Long groupId, Long userId) {
        ChatGroup group = chatGroupRepository.findWithMembersById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        boolean alreadyMember = group.getMembers().stream()
                .anyMatch(member -> member.getId().equals(userId));

        if (!alreadyMember) {
            group.getMembers().add(user);
            group = chatGroupRepository.save(group);
        }

        return mapToGroupDTO(group);
    }

    @Override
    @Transactional
    public GroupResponseDTO removeMemberFromGroup(Long groupId, Long userId) {
        ChatGroup group = chatGroupRepository.findWithMembersById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        if (group.getCreatedBy() != null && group.getCreatedBy().getId().equals(userId)) {
            throw new BadRequestException("The group creator cannot be removed from the group");
        }

        boolean memberRemoved = group.getMembers().removeIf(member -> member.getId().equals(userId));

        if (!memberRemoved) {
            throw new ResourceNotFoundException("User not found in group with id: " + userId);
        }

        group = chatGroupRepository.save(group);
        return mapToGroupDTO(group);
    }

    @Override
    @Transactional
    public void deleteGroup(Long groupId) {
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        messageRepository.deleteByGroupId(groupId);
        chatGroupRepository.delete(group);
    }

    @Override
    public void deleteMessage(Long id) {
        messageRepository.deleteById(id);
    }

    @Override
    public MessageResponseDTO markAsRead(Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        message.setRead(true);

        Message updatedMessage = messageRepository.save(message);
        return mapToDTO(updatedMessage);
    }

    @Override
    public Message updateMessage(Message message) {
        return messageRepository.save(message);
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO getMessageResponseById(Long messageId) {
        Message message = messageRepository.findByIdWithRelations(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));

        return mapToDTO(message);
    }

    private MessageResponseDTO mapToDTO(Message message) {
        return MessageResponseDTO.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFirstName())
                .receiverId(message.getReceiver() != null ? message.getReceiver().getId() : 0L)
                .receiverName(message.getReceiver() != null
                        ? message.getReceiver().getFirstName()
                        : (message.getGroup() != null ? message.getGroup().getName() : null))
                .content(message.getContent())
                .fileUrl(message.getFileUrl())
                .fileType(message.getFileType())
                .isRead(message.isRead())
                .sentAt(message.getSentAt())
                .groupId(message.getGroup() != null ? message.getGroup().getId() : null)
                .flagged(message.isFlagged())
                .build();
    }

    private GroupResponseDTO mapToGroupDTO(ChatGroup group) {
        return GroupResponseDTO.builder()
                .id(group.getId())
                .name(group.getName())
                .creatorId(group.getCreatedBy() != null ? group.getCreatedBy().getId() : null)
                .createdAt(group.getCreatedAt())
                .memberIds(group.getMembers().stream()
                        .map(User::getId)
                        .collect(Collectors.toList()))
                .build();
    }

    private void broadcastDirectMessage(Message savedMessage, MessageResponseDTO response) {
        if (savedMessage.getReceiver() == null) {
            return;
        }

        messagingTemplate.convertAndSend("/topic/messages/" + savedMessage.getReceiver().getId(), response);

        NotificationDTO notification = NotificationDTO.builder()
                .type("New message received")
                .message(savedMessage.getSender().getFirstName())
                .build();

        messagingTemplate.convertAndSend("/topic/notifications/" + savedMessage.getReceiver().getId(), notification);
    }

    private void broadcastGroupMessage(Message savedMessage, MessageResponseDTO response) {
        if (savedMessage.getGroup() == null) {
            return;
        }

        List<Long> memberIds = getGroupMemberIds(savedMessage.getGroup().getId());
        for (Long memberId : memberIds) {
            if (savedMessage.getSender() != null && savedMessage.getSender().getId().equals(memberId)) {
                continue;
            }

            messagingTemplate.convertAndSend("/topic/groups/" + memberId, response);
        }
    }
}