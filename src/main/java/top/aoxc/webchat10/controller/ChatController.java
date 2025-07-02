package top.aoxc.webchat10.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import top.aoxc.webchat10.model.ChatMessage;
import top.aoxc.webchat10.model.User;
import top.aoxc.webchat10.repository.ChatMessageRepository;
import top.aoxc.webchat10.repository.UserRepository;
import top.aoxc.webchat10.controller.UserController;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ChatController {

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    // 存储被禁言用户
    private static final Set<String> mutedUsers = ConcurrentHashMap.newKeySet();

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        // 禁言判断
        if (mutedUsers.contains(chatMessage.getSender())) {
            // 通知自己被禁言
            ChatMessage muteMsg = new ChatMessage();
            muteMsg.setType(ChatMessage.MessageType.MUTE);
            muteMsg.setSender("ADMIN");
            muteMsg.setReceiver(chatMessage.getSender());
            muteMsg.setContent("你已被管理员禁言");
            muteMsg.setTimestamp(LocalDateTime.now());
            messagingTemplate.convertAndSendToUser(chatMessage.getSender(), "/queue/private", muteMsg);
            return;
        }
        chatMessage.setTimestamp(LocalDateTime.now());
        chatMessageRepository.save(chatMessage);

        if ("public".equals(chatMessage.getReceiver())) {
            // 发送到公共频道
            messagingTemplate.convertAndSend("/topic/public", chatMessage);
        } else {
            // 发送到指定用户的私有队列
            messagingTemplate.convertAndSendToUser(chatMessage.getReceiver(), "/queue/private", chatMessage);
            // 同时给自己也发一份，用于在自己的聊天窗口显示
            messagingTemplate.convertAndSendToUser(chatMessage.getSender(), "/queue/private", chatMessage);
        }
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // 将用户名添加到 WebSocket session
        Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("username", chatMessage.getSender());
        headerAccessor.getSessionAttributes().put("loginTime", LocalDateTime.now());
        headerAccessor.getSessionAttributes().put("sessionId", headerAccessor.getSessionId());
        // 向公共频道广播新用户加入
        messagingTemplate.convertAndSend("/topic/public", chatMessage);
        // 记录在线用户
        User user = userRepository.findByUsername(chatMessage.getSender()).orElse(null);
        if (user != null) {
            UserController.userOnline(user.getUsername(), LocalDateTime.now().toString(), headerAccessor.getSessionId(), user.getRole());
        }
    }

    @MessageMapping("/admin.kick")
    public void kickUser(@Payload ChatMessage kickRequest, SimpMessageHeaderAccessor headerAccessor) {
        String adminUsername = (String) headerAccessor.getSessionAttributes().get("username");
        User admin = userRepository.findByUsername(adminUsername).orElse(null);

        // 验证操作者是否是管理员
        if (admin != null && "ADMIN".equals(admin.getRole())) {
            String userToKick = kickRequest.getSender();

            // 创建踢出消息
            ChatMessage kickMessage = new ChatMessage();
            kickMessage.setType(ChatMessage.MessageType.KICK);
            kickMessage.setSender("ADMIN");
            kickMessage.setReceiver(userToKick);
            kickMessage.setContent("You have been kicked by an administrator.");
            kickMessage.setTimestamp(LocalDateTime.now());

            // 发送踢出通知给被踢用户
            messagingTemplate.convertAndSendToUser(userToKick, "/queue/private", kickMessage);

            // 可以在这里添加逻辑来强制断开用户的连接，但这比较复杂。
            // 一个简单的方法是客户端收到 KICK 消息后主动断开。
        }
    }

    @MessageMapping("/admin.mute")
    public void muteUser(@Payload ChatMessage muteRequest, SimpMessageHeaderAccessor headerAccessor) {
        String adminUsername = (String) headerAccessor.getSessionAttributes().get("username");
        User admin = userRepository.findByUsername(adminUsername).orElse(null);
        if (admin != null && "ADMIN".equals(admin.getRole())) {
            String userToMute = muteRequest.getSender();
            boolean isMuted = mutedUsers.contains(userToMute);
            ChatMessage muteMsg = new ChatMessage();
            muteMsg.setType(ChatMessage.MessageType.MUTE);
            muteMsg.setSender("ADMIN");
            muteMsg.setReceiver(userToMute);
            muteMsg.setTimestamp(LocalDateTime.now());
            if (isMuted) {
                mutedUsers.remove(userToMute);
                muteMsg.setContent("你已被管理员解除禁言");
            } else {
                mutedUsers.add(userToMute);
                muteMsg.setContent("你已被管理员禁言");
            }
            // messagingTemplate.convertAndSendToUser(userToMute, "/queue/private", muteMsg);
            // 只推送一次到公共频道，内容区分
            messagingTemplate.convertAndSend("/topic/public", muteMsg);
        }
    }

    // 禁言功能类似，需要一个地方存储被禁言的用户列表（例如一个 ConcurrentHashMap 或数据库字段）
    // 然后在 sendMessage 方法中检查用户是否被禁言。这里为了简化，暂时不实现。
}