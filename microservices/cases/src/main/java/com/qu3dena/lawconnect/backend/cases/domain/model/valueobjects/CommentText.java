package com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects;

/**
 * Value object representing the text of a comment.
 * <p>
 * This class is immutable and ensures that the comment text is not null or empty.
 * It is designed to encapsulate the logic for validating and handling comment texts.
 *
 * <p>Use this value object to represent and validate comment text in a consistent manner.</p>
 *
 * @author LawConnect Team
 * @since 1.0
 */
public record CommentText(String comment) {
    /**
     * Constructs a new {@code CommentText} instance.
     *
     * @param comment the text of the comment
     */
    public CommentText {
        if (comment == null || comment.isEmpty())
            throw new IllegalArgumentException("comment cannot be null or empty");
    }
}
