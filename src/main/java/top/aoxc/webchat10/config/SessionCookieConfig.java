package top.aoxc.webchat10.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class SessionCookieConfig {

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        // 设置 SameSite 属性为 None，允许跨站点发送 cookie
        serializer.setSameSite("None");
        // 当 SameSite=None 时，必须同时设置 Secure 为 true，表示 cookie 只能通过 HTTPS 发送
        serializer.setUseSecureCookie(true);
        // 设置 cookie 的路径，通常设置为根路径以便在所有子路径下都可用
        serializer.setCookiePath("/");
        // 如果你的前端和后端在不同的子域名下（例如，前端是 app.example.com，后端是 api.example.com），
        // 你可能需要设置 domainName。如果它们在完全不同的域名下，则不需要设置。
        // serializer.setDomainName("你的主域名.com"); // 例如: "example.com"
        return serializer;
    }
}