package ru.practicum.core.dto.comment.params;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.core.utils.PageableFactory;

public record CommentParams(
        Long eventId,
        Pageable pageable
) {

    public static CommentParams of(Long eventId, Integer from, Integer size) {
        return new CommentParams(
                eventId,
                PageableFactory.offset(from, size, Sort.by("id"))
        );
    }
}
