package ru.practicum.core.dto.comment.params;

import ru.practicum.core.dto.comment.request.UpdateCommentBody;

public record UpdateCommentParams(
        Long commentId,
        String text
) {
    public static UpdateCommentParams of(Long commentId, UpdateCommentBody body) {
        return new UpdateCommentParams(
                commentId,
                body.text());
    }
}
