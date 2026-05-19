package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.core.dto.TEMPORARY.State;
import ru.practicum.core.dto.comment.params.CommentParams;
import ru.practicum.core.dto.comment.params.CreateCommentParams;
import ru.practicum.core.dto.comment.params.PrivateUpdateCommentParams;
import ru.practicum.core.dto.comment.params.UpdateCommentParams;
import ru.practicum.core.dto.comment.response.CommentDto;
import ru.practicum.core.dto.event.response.EventDtoInternal;
import ru.practicum.core.dto.user.response.UserShortDto;
import ru.practicum.core.exception.ConflictException;
import ru.practicum.core.feign.client.EventFeignClient;
import ru.practicum.core.feign.client.UserFeignClient;
import ru.practicum.core.utils.BaseService;
import ru.practicum.core.utils.EntityName;
import ru.practicum.model.Comment;
import ru.practicum.model.mapper.CommentMapper;
import ru.practicum.repository.CommentRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends BaseService implements CommentService {
    private final CommentRepository commentRepository;

    private final CommentMapper mapper;

    private final EventFeignClient eventFeignClient;
    private final UserFeignClient userFeignClient;


    @Override
    public CommentDto create(CreateCommentParams params) {
        UserShortDto author = findUserOrThrow(Set.of(params.authorId()));
        EventDtoInternal event = eventFeignClient.get(params.eventId());

        if (event.state() != State.PUBLISHED) {
            throw new ConflictException("Event %d is not published to be commented".formatted(params.eventId()));
        }

        Comment comment = mapper.toEntity(params);
        comment.setAuthorId(author.id());
        comment.setEventId(event.id());

        commentRepository.save(comment);
        return mapper.toDto(comment, author);
    }

    @Override
    public CommentDto get(Long id) {
        Comment comment = findCommentOrThrow(id);
        return mapper.toDto(comment, findUserOrThrow(Set.of(comment.getAuthorId())));
    }

    @Override
    public List<CommentDto> get(CommentParams params) {
        List<Comment> comments = commentRepository.findByEventId(params.eventId(), params.pageable())
                .getContent();

        Set<Long> authorsIds = comments.stream()
                .map(Comment::getAuthorId)
                .collect(Collectors.toSet());

        Map<Long, UserShortDto> authors = userFeignClient.getUserShortDto(authorsIds).stream()
                .collect(Collectors.toMap(
                        UserShortDto::id, Function.identity()
                ));

        return comments.stream()
                .map(comment -> mapper.toDto(
                        comment,
                        authors.get(comment.getAuthorId())))
                .toList();
    }

    @Override
    public void delete(Long id) {
        commentRepository.deleteById(id);
    }

    @Override
    public CommentDto update(UpdateCommentParams params) {
        Comment comment = findCommentOrThrow(params.commentId());
        UserShortDto author = findUserOrThrow(Set.of(comment.getAuthorId()));

        mapper.updateEntity(params, comment);
        commentRepository.save(comment);

        return mapper.toDto(comment, author);
    }

    @Override
    public CommentDto updatePrivate(PrivateUpdateCommentParams dto) {
        Comment comment = findCommentOrThrow(dto.commentId());
        UserShortDto author = findUserOrThrow(Set.of(comment.getAuthorId()));

        if (dto.userId() != null && !comment.getAuthorId().equals(dto.userId())) {
            throw new ConflictException("You are not the author of this comment");
        }

        mapper.updatePrivateEntity(dto, comment);
        commentRepository.save(comment);

        return mapper.toDto(comment, author);
    }

    @Override
    public void deleteByUser(Long userId, Long commentId) {
        Comment comment = findCommentOrThrow(commentId);

        if (!comment.getAuthorId().equals(userId)) {
            throw new ConflictException("You are not the author of this comment");
        }
        commentRepository.delete(comment);
    }

    private Comment findCommentOrThrow(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> throwNotFound(commentId, EntityName.COMMENT));
    }

    private UserShortDto findUserOrThrow(Set<Long> userIds) {
        return userFeignClient.getUserShortDto(userIds).getFirst();
    }
}
