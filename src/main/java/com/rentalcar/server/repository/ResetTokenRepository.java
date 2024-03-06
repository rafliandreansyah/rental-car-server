package com.rentalcar.server.repository;

import com.rentalcar.server.entity.ResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ResetTokenRepository extends JpaRepository<ResetToken, UUID> {

    Optional<ResetToken> findByUserId(UUID userId);

    Optional<ResetToken> findByToken(String token);

    Optional<ResetToken> findByUserIdAndToken(UUID userId, String token);

}
