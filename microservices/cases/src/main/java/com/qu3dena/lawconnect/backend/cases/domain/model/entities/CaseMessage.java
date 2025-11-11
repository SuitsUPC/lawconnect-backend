package com.qu3dena.lawconnect.backend.cases.domain.model.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a private message between client and lawyer in a case.
 */
@Data
@Entity
@NoArgsConstructor
@Table(name = "case_messages")
@EntityListeners(AuditingEntityListener.class)
public class CaseMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "case_id", nullable = false)
    private UUID caseId;

    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    @Column(nullable = false, length = 1000)
    private String content;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_read")
    private Boolean isRead = false;

    public CaseMessage(UUID caseId, UUID senderId, String content) {
        this.caseId = caseId;
        this.senderId = senderId;
        this.content = content;
        this.isRead = false;
    }
}

