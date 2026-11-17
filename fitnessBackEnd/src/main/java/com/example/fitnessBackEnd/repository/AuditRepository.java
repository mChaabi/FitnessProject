package com.example.fitnessBackEnd.repository;

import com.example.fitnessBackEnd.entity.AuditEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditRepository extends JpaRepository<AuditEntry, Long> {

    @Query("""
        select a from AuditEntry a
        where a.createdAt >= :from and a.createdAt < :to
          and (:type = '' or a.entityType = :type)
          and (:q = '' or lower(a.actor) like lower(concat('%', :q, '%'))
                       or lower(a.summary) like lower(concat('%', :q, '%')))
        """)
    Page<AuditEntry> search(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
                            @Param("type") String type, @Param("q") String q, Pageable page);

    @Query("select a.entityType, count(a) from AuditEntry a where a.createdAt >= :from and a.createdAt < :to group by a.entityType")
    List<Object[]> countByType(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Transactional
    @Modifying
    @Query("delete from AuditEntry a where a.createdAt < :before")
    int purge(@Param("before") LocalDateTime before);
}