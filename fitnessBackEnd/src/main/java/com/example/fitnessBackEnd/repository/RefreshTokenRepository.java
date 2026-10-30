package com.example.fitnessBackEnd.repository;

import com.example.fitnessBackEnd.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @Query("select t from RefreshToken t join fetch t.user where t.tokenHash = :h")
    Optional<RefreshToken> findByHash(@Param("h") String h);

    @Query("select t.revokedAt from RefreshToken t where t.id = :id")
    LocalDateTime revokedAt(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update RefreshToken t set t.revokedAt = :now where t.id = :id and t.revokedAt is null")
    int revokeOne(@Param("id") Long id, @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update RefreshToken t set t.revokedAt = :ancient where t.familyId = :f")
    int revokeFamily(@Param("f") String familyId, @Param("ancient") LocalDateTime ancient);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update RefreshToken t set t.revokedAt = :ancient where t.user.id = :uid")
    int revokeAllOfUser(@Param("uid") Long userId, @Param("ancient") LocalDateTime ancient);

    @Modifying
    @Query("delete from RefreshToken t where t.expiresAt < :before")
    int purge(@Param("before") LocalDateTime before);
}