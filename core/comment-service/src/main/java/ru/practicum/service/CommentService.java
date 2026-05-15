package ru.practicum.service;

import ru.practicum.core.dto.comment.params.CommentParams;
import ru.practicum.core.dto.comment.params.CreateCommentParams;
import ru.practicum.core.dto.comment.params.PrivateUpdateCommentParams;
import ru.practicum.core.dto.comment.params.UpdateCommentParams;
import ru.practicum.core.dto.comment.response.CommentDto;

import java.util.List;

public interface CommentService {

    CommentDto create(CreateCommentParams params);

    CommentDto get(Long id);

    List<CommentDto> get(CommentParams params);

    void delete(Long id);

    CommentDto update(UpdateCommentParams params);

    CommentDto updatePrivate(PrivateUpdateCommentParams params);

    void deleteByUser(Long userId, Long commentId);
}
