package ru.practicum.core.dto.comment.params;

import ru.practicum.core.dto.comment.request.UpdateCommentBody;

public record PrivateUpdateCommentParams(
        Long userId,
        Long commentId,
        String text
) {
    public static PrivateUpdateCommentParams of(
            Long userId,
            Long commentId,
            UpdateCommentBody body
    ) {
        return new PrivateUpdateCommentParams(
                userId,
                commentId,
                body.text()
        );
    }
}
