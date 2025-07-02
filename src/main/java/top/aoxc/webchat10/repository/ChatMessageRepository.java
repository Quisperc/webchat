package top.aoxc.webchat10.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import top.aoxc.webchat10.model.ChatMessage;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // 查询公共聊天记录
    List<ChatMessage> findByReceiverOrderByTimestampAsc(String receiver);

    // 查询两个用户之间的私聊记录
    @Query("SELECT m FROM ChatMessage m WHERE (m.sender = ?1 AND m.receiver = ?2) OR (m.sender = ?2 AND m.receiver = ?1) ORDER BY m.timestamp ASC")
    List<ChatMessage> findPrivateMessages(String user1, String user2);
}