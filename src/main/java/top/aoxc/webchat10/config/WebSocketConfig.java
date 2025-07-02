package top.aoxc.webchat10.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Map;
import top.aoxc.webchat10.model.User;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册一个 STOMP 端点，客户端将使用它来连接 WebSocket 服务器
        // withSockJS() 是为了支持不兼容 WebSocket 的浏览器
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .setHandshakeHandler(handshakeHandler())
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 配置消息代理
        // 应用程序（服务器）的目标前缀，客户端发送消息到这些前缀的目的地时，会路由到 @MessageMapping 注解的方法
        registry.setApplicationDestinationPrefixes("/app");
        // 启用一个简单的主题代理，目的地前缀为 /topic 和 /queue
        // /topic 用于公共消息，/queue 用于私有消息
        registry.enableSimpleBroker("/topic", "/queue");
        // 设置用户目的地的广播前缀，用于点对点消息
        registry.setUserDestinationPrefix("/user");
    }

    @Bean
    public HandshakeInterceptor httpSessionHandshakeInterceptor() {
        return new HttpSessionHandshakeInterceptor();
    }

    @Bean
    public DefaultHandshakeHandler handshakeHandler() {
        return new DefaultHandshakeHandler() {
            @Override
            protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
                String username = null;
                if (request instanceof ServletServerHttpRequest) {
                    HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
                    username = servletRequest.getParameter("username");
                    if (username == null) {
                        username = servletRequest.getHeader("X-Username");
                    }
                }
                if (username != null && !username.isEmpty()) {
                    System.out.println("[WebSocket握手] 直接用用户名: " + username);
                    String finalUsername = username;
                    return () -> finalUsername;
                }
                return super.determineUser(request, wsHandler, attributes);
            }
        };
    }
}