package com.qu3dena.lawconnect.backend.cases.domain.model.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a document attached to a case.
 */
@Data
@Entity
@NoArgsConstructor
@Table(name = "documents")
@EntityListeners(AuditingEntityListener.class)
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "case_id", nullable = false)
    private UUID caseId;

    @Column(name = "uploaded_by", nullable = false)
    private UUID uploadedBy;

    @Column(nullable = false)
    private String filename;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_type")
    private String fileType;

    @Lob
    @Column(name = "file_content", columnDefinition = "LONGBLOB")
    private byte[] fileContent;

    @CreatedDate
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    public Document(UUID caseId, UUID uploadedBy, String filename, String fileUrl, Long fileSize, String fileType, byte[] fileContent) {
        this.caseId = caseId;
        this.uploadedBy = uploadedBy;
        this.filename = filename;
        this.fileUrl = fileUrl;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.fileContent = fileContent;
    }
}

