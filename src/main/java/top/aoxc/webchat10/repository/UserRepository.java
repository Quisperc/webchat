package top.aoxc.webchat10.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import top.aoxc.webchat10.model.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}