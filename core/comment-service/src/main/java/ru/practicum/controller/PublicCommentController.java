package ru.practicum.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.core.dto.comment.params.CommentParams;
import ru.practicum.core.dto.comment.response.CommentDto;
import ru.practicum.core.utils.ApiPaths;
import ru.practicum.service.CommentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.Public.COMMENTS)
@Validated
@Slf4j
public class PublicCommentController {
    private final CommentService service;

    @GetMapping
    public List<CommentDto> get(@PathVariable @Positive Long eventId,
                                @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                @RequestParam(defaultValue = "20") @Positive Integer size) {

        log.info("PUBLIC: Get comments by Event {} with params: from {}, size {}", eventId, from, size);
        CommentParams params = CommentParams.of(eventId, from, size);
        List<CommentDto> result = service.get(params);
        log.info("PUBLIC: Found {} comments", result.size());
        return result;
    }
}
