package top.aoxc.webchat10.listener;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import top.aoxc.webchat10.model.ChatMessage;
import top.aoxc.webchat10.controller.UserController;
import top.aoxc.webchat10.repository.ChatMessageRepository;

import java.time.LocalDateTime;
import java.util.Objects;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        logger.info("Received a new web socket connection");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = (String) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("username");
        if (username != null) {
            logger.info("User Disconnected : " + username);

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setType(ChatMessage.MessageType.LEAVE);
            chatMessage.setSender(username);

            // 向公共频道广播用户离开的消息
            messagingTemplate.convertAndSend("/topic/public", chatMessage);
            // 移除在线用户
            UserController.userOffline(username);
            // 保存LEAVE消息
            chatMessage.setTimestamp(LocalDateTime.now());
            chatMessage.setReceiver("public");
            chatMessage.setType(ChatMessage.MessageType.LEAVE);
            chatMessageRepository.save(chatMessage);
            // 发送到公共频道
            messagingTemplate.convertAndSend("/topic/public", chatMessage);
        }
    }
}