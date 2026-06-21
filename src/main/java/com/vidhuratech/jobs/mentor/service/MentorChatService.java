package com.vidhuratech.jobs.mentor.service;

import com.vidhuratech.jobs.mentor.entity.MentorChatMessage;
import com.vidhuratech.jobs.mentor.entity.MentorStudentRelation;
import com.vidhuratech.jobs.mentor.repository.MentorChatMessageRepository;
import com.vidhuratech.jobs.mentor.repository.MentorStudentRelationRepository;
import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MentorChatService {

    private final MentorChatMessageRepository chatRepo;
    private final MentorStudentRelationRepository relationRepo;
    private final UserRepository userRepo;

    @Transactional(readOnly = true)
    public Map<String, Object> getChatMessages(Long userId, Long relationId) {
        MentorStudentRelation relation = relationRepo.findByIdWithMentorAndStudent(relationId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found"));

        // Validate that user is either the student or the mentor in this relation
        boolean isMentor = relation.getMentor().getId().equals(userId);
        boolean isStudent = relation.getStudent().getId().equals(userId);
        if (!isMentor && !isStudent) {
            throw new RuntimeException("Unauthorized: You are not a participant in this chat");
        }

        User recipient = isMentor ? relation.getStudent() : relation.getMentor();

        Map<String, Object> result = new HashMap<>();
        result.put("relationId", relation.getId());
        result.put("recipientName", recipient.getName());
        result.put("recipientEmail", recipient.getEmail());
        result.put("recipientAvatar", recipient.getName() != null && !recipient.getName().isEmpty() 
                ? recipient.getName().substring(0, 1).toUpperCase() : "U");
        result.put("recipientRole", isMentor ? "Student" : "Mentor");

        List<MentorChatMessage> messages = chatRepo.findAllByRelationIdOrderByCreatedAtAsc(relationId);
        List<Map<String, Object>> mappedMessages = messages.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("senderId", m.getSender().getId());
            map.put("senderName", m.getSender().getName());
            map.put("messageText", m.getMessageText());
            map.put("createdAt", m.getCreatedAt());
            return map;
        }).collect(Collectors.toList());

        result.put("messages", mappedMessages);
        return result;
    }

    @Transactional
    public Map<String, Object> sendChatMessage(Long userId, Long relationId, String messageText) {
        if (messageText == null || messageText.trim().isEmpty()) {
            throw new RuntimeException("Message content cannot be empty");
        }

        MentorStudentRelation relation = relationRepo.findByIdWithMentorAndStudent(relationId)
                .orElseThrow(() -> new RuntimeException("Mentorship relationship not found"));

        // Validate participation
        boolean isMentor = relation.getMentor().getId().equals(userId);
        boolean isStudent = relation.getStudent().getId().equals(userId);
        if (!isMentor && !isStudent) {
            throw new RuntimeException("Unauthorized: You are not a participant in this chat");
        }

        User sender = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Sender user not found"));

        MentorChatMessage msg = new MentorChatMessage();
        msg.setRelation(relation);
        msg.setSender(sender);
        msg.setMessageText(messageText.trim());
        chatRepo.save(msg);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Message sent successfully");
        response.put("id", msg.getId());
        response.put("senderId", sender.getId());
        response.put("senderName", sender.getName());
        response.put("messageText", msg.getMessageText());
        response.put("createdAt", msg.getCreatedAt());
        return response;
    }
}
