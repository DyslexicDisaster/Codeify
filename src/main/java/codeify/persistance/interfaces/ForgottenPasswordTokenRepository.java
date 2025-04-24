package codeify.persistance.interfaces;

import codeify.entities.ForgottenPasswordToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ForgottenPasswordTokenRepository extends JpaRepository<ForgottenPasswordToken, Long> {
    ForgottenPasswordToken findByToken(String token);

    void deleteByExpiryDateBefore(LocalDateTime now);

    ForgottenPasswordToken save(ForgottenPasswordToken token);
}
