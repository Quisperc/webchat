package top.aoxc.webchat10.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.aoxc.webchat10.model.ChatMessage;
import top.aoxc.webchat10.model.User;
import top.aoxc.webchat10.repository.ChatMessageRepository;
import top.aoxc.webchat10.repository.UserRepository;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    // 在线用户信息存储（简单实现，生产环境建议用分布式缓存等）
    private static final Map<String, Map<String, Object>> onlineUserMap = new HashMap<>();

    // 用户上线时调用
    public static void userOnline(String username, String loginTime, String sessionId, String role) {
        // if (onlineUserMap.containsKey(username)) {
        //     onlineUserMap.remove(username);
        // }
        Map<String, Object> info = new HashMap<>();
        info.put("username", username);
        info.put("loginTime", loginTime);
        info.put("sessionId", sessionId);
        info.put("role", role);
        onlineUserMap.put(username, info);
    }

    // 用户下线时调用
    public static void userOffline(String username) {
        onlineUserMap.remove(username);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username is already taken!");
        }
        // 简单处理，实际项目中密码需要加密
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user, HttpSession session) {
        Optional<User> dbUser = userRepository.findByUsername(user.getUsername());
        if (dbUser.isPresent() && dbUser.get().getPassword().equals(user.getPassword())) {
            session.setAttribute("user", dbUser.get());
            return ResponseEntity.ok(dbUser.get());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }

    @GetMapping("/user/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
    }

    @GetMapping("/history/{targetUser}")
    public ResponseEntity<List<ChatMessage>> getHistory(@PathVariable String targetUser, @RequestParam String currentUser) {
        List<ChatMessage> history;
        if ("public".equalsIgnoreCase(targetUser)) {
            history = chatMessageRepository.findByReceiverOrderByTimestampAsc("public");
        } else {
            history = chatMessageRepository.findPrivateMessages(currentUser, targetUser);
        }
        return ResponseEntity.ok(history);
    }

    @GetMapping("/online-users")
    public ResponseEntity<?> getOnlineUsers() {
        return ResponseEntity.ok(onlineUserMap.values());
    }
}