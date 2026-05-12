package ru.practicum.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.core.dto.user.request.NewUserRequest;
import ru.practicum.core.dto.user.response.UserDto;
import ru.practicum.core.dto.user.request.UserSearchRequest;
import ru.practicum.core.dto.user.response.UserShortDto;
import ru.practicum.core.exception.ConflictException;
import ru.practicum.core.exception.NotFoundException;
import ru.practicum.core.utils.BaseService;
import ru.practicum.core.mapper.UserMapper;
import ru.practicum.core.model.User;
import ru.practicum.core.repository.UserRepository;
import ru.practicum.core.utils.EntityName;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends BaseService implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getUsers(UserSearchRequest request) {
        Pageable pageable = PageRequest.of(request.from() / request.size(), request.size());

        Page<User> page;
        if (request.ids() == null || request.ids().isEmpty()) {
            page = userRepository.findAll(pageable);
        } else {
            page = userRepository.findAllByIdIn(request.ids(), pageable);
        }

        return page.stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    public UserDto getUser(Long userId) {
        return userRepository.findById(userId)
                .map(userMapper::toDto)
                .orElseThrow(() -> new NotFoundException("User with id %s not found".formatted(userId)));
    }

    @Override
    public List<UserShortDto> getUserShort(Set<Long> userIds) {
        return userRepository.findAllByIdIn(new ArrayList<>(userIds), Pageable.unpaged())
                .map(userMapper::toShortDto)
                .toList();
    }

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Пользователь с email " + request.email() + " уже существует");
        }

        User user = userMapper.toEntity(request);
        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw throwNotFound(userId, EntityName.USER);
        }
        userRepository.deleteById(userId);
    }
}