package ru.practicum.model.mapper;

import org.mapstruct.*;
import ru.practicum.core.dto.comment.params.CreateCommentParams;
import ru.practicum.core.dto.comment.params.PrivateUpdateCommentParams;
import ru.practicum.core.dto.comment.params.UpdateCommentParams;
import ru.practicum.core.dto.comment.response.CommentDto;
import ru.practicum.core.dto.user.response.UserShortDto;
import ru.practicum.model.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {


    @Mapping(target = "id", source = "comment.id")
    @Mapping(target = "author", source = "author")
    CommentDto toDto(Comment comment, UserShortDto author);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    Comment toEntity(CreateCommentParams dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "eventId", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateCommentParams dto, @MappingTarget Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "eventId", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePrivateEntity(PrivateUpdateCommentParams dto, @MappingTarget Comment comment);
}
