package ru.practicum.core.dto.comment.params;

import ru.practicum.core.dto.comment.request.CreateCommentBody;

public record CreateCommentParams(
        Long authorId,
        Long eventId,
        String text
) {
    public static CreateCommentParams of(Long authorId, Long eventId, CreateCommentBody body) {
        return new CreateCommentParams(
                authorId,
                eventId,
                body.text());
    }
}
