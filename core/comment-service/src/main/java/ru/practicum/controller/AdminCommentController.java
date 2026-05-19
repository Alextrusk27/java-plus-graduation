package ru.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.core.dto.comment.params.UpdateCommentParams;
import ru.practicum.core.dto.comment.request.UpdateCommentBody;
import ru.practicum.core.dto.comment.response.CommentDto;
import ru.practicum.core.utils.ApiPaths;
import ru.practicum.service.CommentService;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.Admin.COMMENTS)
@Validated
@Slf4j
public class AdminCommentController {
    private final CommentService commentService;

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable @Positive Long commentId) {

        log.info("ADMIN: Delete comment {}", commentId);
        commentService.delete(commentId);
        log.debug("ADMIN: Comment {} deleted", commentId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto update(
            @PathVariable @Positive Long commentId,
            @RequestBody @Valid UpdateCommentBody body) {

        log.info("ADMIN: Update comment {}", commentId);
        UpdateCommentParams dto = UpdateCommentParams.of(commentId, body);
        CommentDto result = commentService.update(dto);
        log.debug("ADMIN: Comment {} updated", commentId);
        return result;
    }

    @GetMapping("/{commentId}")
    public CommentDto get(@PathVariable @Positive Long commentId) {

        log.info("PUBLIC: Get comment by id {}", commentId);
        CommentDto result = commentService.get(commentId);
        log.debug("PUBLIC: Found comment {}", result.id());
        return result;
    }
}
