package com.qu3dena.lawconnect.backend.cases.domain.model.entities;

import com.qu3dena.lawconnect.backend.cases.domain.model.aggregates.CaseAggregate;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CommentText;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CommentType;
import com.qu3dena.lawconnect.backend.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Entity representing a comment in the system.
 * <p>
 * This class is used to manage comments associated with cases and authors.
 * It extends {@link AuditableModel} to include audit information such as creation
 * and modification timestamps.
 *
 * <p>Each comment is linked to a specific case and author, and contains the text
 * of the comment encapsulated in {@link CommentText}.</p>
 *
 * <p>Use this entity to persist and retrieve comment data from the database.</p>
 *
 * @author LawConnect Team
 * @since 1.0
 */
@Data
@Entity
@NoArgsConstructor
@Table(name = "comments")
@EqualsAndHashCode(callSuper = true, exclude = "legalCase")
public class Comment extends AuditableModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The legal case associated with this comment.
     * <p>
     * This field establishes a many-to-one relationship with the {@link CaseAggregate} entity.
     * </p>
     */
    @ManyToOne
    @JoinColumn(name = "case_id", nullable = false)
    private CaseAggregate legalCase;

    /**
     * The unique identifier of the author who created the comment.
     */
    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    /**
     * The text of the comment.
     * <p>
     * This field is encapsulated in the {@link CommentText} value object.
     * </p>
     */
    @Embedded
    @AttributeOverride(name = "comment", column = @Column(name = "comment"))
    private CommentText text;

    /**
     * The type of the comment.
     * <p>
     * This field is an enumeration that indicates the nature of the comment (e.g., general, question, etc.).
     * </p>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private CommentType type;

    /**
     * Constructs a new Comment instance with the specified legal case, author ID, and text.
     *
     * @param legalCase the legal case associated with the comment
     * @param authorId  the unique identifier of the author
     * @param text      the text of the comment
     * @param type      the type of the comment
     */
    public Comment(CaseAggregate legalCase, UUID authorId, CommentText text, CommentType type) {
        this.legalCase = legalCase;
        this.authorId = authorId;
        this.text = text;
        this.type = type;
    }

    /**
     * Retrieves the unique identifier of the case associated with this comment.
     *
     * @return the case ID
     */
    public UUID getCaseId() {
        return legalCase.getId();
    }

    /**
     * Retrieves the unique identifier of the author associated with the case of this comment.
     *
     * @return the author ID
     */
    public UUID getAuthorId() {
        return legalCase.getClientId();
    }

    /**
     * Creates a new Comment instance with the specified text, legal case, and author ID.
     *
     * @param text      the text of the comment
     * @param legalCase the legal case associated with the comment
     * @param authorId  the unique identifier of the author
     * @param type      the type of the comment
     * @return a new Comment instance
     */
    public static Comment create(CommentText text, CaseAggregate legalCase, UUID authorId, CommentType type) {
        return new Comment(legalCase, authorId, text, type);
    }
}
