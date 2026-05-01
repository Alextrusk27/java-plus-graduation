package ru.practicum.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.core.dto.user.NewUserRequest;
import ru.practicum.core.dto.user.UserDto;
import ru.practicum.core.dto.user.UserSearchRequest;
import ru.practicum.core.exception.ConflictException;
import ru.practicum.core.exception.NotFoundException;
import ru.practicum.core.mapper.UserMapper;
import ru.practicum.core.model.User;
import ru.practicum.core.repository.UserRepository;
import ru.practicum.core.service.UserServiceImpl;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    private final Random random = new Random();

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Nested
    @DisplayName("getUsers() - Get users with filters")
    class GetUsersTests {

        @Test
        @DisplayName("Should return all users when no IDs provided")
        void getUsers_whenNoIds_thenFindAll() {
            UserSearchRequest request = new UserSearchRequest(null, 0, 20);
            Pageable pageable = PageRequest.of(0, 20);

            User user1 = getNewUser();
            User user2 = getNewUser();
            Page<User> page = new PageImpl<>(List.of(user1, user2));

            UserDto dto1 = new UserDto(user1.getId(), user1.getName(), user1.getEmail());
            UserDto dto2 = new UserDto(user2.getId(), user2.getName(), user2.getEmail());

            when(userRepository.findAll(pageable)).thenReturn(page);
            when(userMapper.toDto(user1)).thenReturn(dto1);
            when(userMapper.toDto(user2)).thenReturn(dto2);

            List<UserDto> result = userService.getUsers(request);

            assertThat(result).hasSize(2);
            assertThat(result).containsExactly(dto1, dto2);

            verify(userRepository).findAll(pageable);
            verify(userRepository, never()).findAllByIdIn(any(), any());
            verify(userMapper, times(2)).toDto(any(User.class));
        }

        @Test
        @DisplayName("Should return users by IDs when IDs are provided")
        void getUsers_whenIdsProvided_thenFindAllByIdIn() {
            List<Long> ids = List.of(1L, 2L, 3L);
            UserSearchRequest request = new UserSearchRequest(ids, 0, 20);
            Pageable pageable = PageRequest.of(0, 20);

            User user1 = getNewUser();
            User user2 = getNewUser();
            Page<User> page = new PageImpl<>(List.of(user1, user2));

            UserDto dto1 = new UserDto(user1.getId(), user1.getName(), user1.getEmail());
            UserDto dto2 = new UserDto(user2.getId(), user2.getName(), user2.getEmail());

            when(userRepository.findAllByIdIn(ids, pageable)).thenReturn(page);
            when(userMapper.toDto(user1)).thenReturn(dto1);
            when(userMapper.toDto(user2)).thenReturn(dto2);

            List<UserDto> result = userService.getUsers(request);

            assertThat(result).hasSize(2);
            assertThat(result).containsExactly(dto1, dto2);

            verify(userRepository).findAllByIdIn(ids, pageable);
            verify(userRepository, never()).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("Should return all users when IDs list is empty")
        void getUsers_whenIdsEmpty_thenFindAll() {
            UserSearchRequest request = new UserSearchRequest(List.of(), 0, 20);
            Pageable pageable = PageRequest.of(0, 20);
            Page<User> page = Page.empty();

            when(userRepository.findAll(pageable)).thenReturn(page);

            List<UserDto> result = userService.getUsers(request);

            assertThat(result).isEmpty();

            verify(userRepository).findAll(pageable);
            verify(userRepository, never()).findAllByIdIn(any(), any());
        }

        @Test
        @DisplayName("Should calculate pagination correctly from from/size parameters")
        void getUsers_verifyPaginationCalculation() {
            UserSearchRequest request = new UserSearchRequest(null, 10, 5);

            when(userRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

            userService.getUsers(request);

            verify(userRepository).findAll(PageRequest.of(2, 5));
        }
    }

    @Nested
    @DisplayName("createUser() - Create new user")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully when email is unique")
        void createUser_whenEmailNotExists_thenSuccess() {
            NewUserRequest request = new NewUserRequest("test@example.com", "Test User");
            User user = User.builder()
                    .id(random.nextLong(1L, 100L))
                    .email(request.email())
                    .name(request.name())
                    .build();
            User savedUser = User.builder()
                    .id(user.getId())
                    .email(request.email())
                    .name(request.name())
                    .build();
            UserDto expectedDto = new UserDto(savedUser.getId(), savedUser.getName(), savedUser.getEmail());

            when(userRepository.existsByEmail(request.email())).thenReturn(false);
            when(userMapper.toEntity(request)).thenReturn(user);
            when(userRepository.save(user)).thenReturn(savedUser);
            when(userMapper.toDto(savedUser)).thenReturn(expectedDto);

            UserDto result = userService.createUser(request);

            assertThat(result).isEqualTo(expectedDto);
            assertThat(result.email()).isEqualTo(request.email());
            assertThat(result.name()).isEqualTo(request.name());

            verify(userRepository).existsByEmail(request.email());
            verify(userMapper).toEntity(request);
            verify(userRepository).save(user);
            verify(userMapper).toDto(savedUser);
        }

        @Test
        @DisplayName("Should throw ConflictException when email already exists")
        void createUser_whenEmailExists_thenThrowConflictException() {
            NewUserRequest request = new NewUserRequest("existing@example.com", "Test User");

            when(userRepository.existsByEmail(request.email())).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Пользователь с email " + request.email() + " уже существует");

            verify(userRepository).existsByEmail(request.email());
            verify(userMapper, never()).toEntity(any());
            verify(userRepository, never()).save(any());
            verify(userMapper, never()).toDto(any(User.class));
        }
    }

    @Nested
    @DisplayName("deleteUser() - Delete user by ID")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user successfully when user exists")
        void deleteUser_whenUserExists_thenSuccess() {
            Long userId = random.nextLong(1L, 100L);

            when(userRepository.existsById(userId)).thenReturn(true);
            doNothing().when(userRepository).deleteById(userId);

            userService.deleteUser(userId);

            verify(userRepository).existsById(userId);
            verify(userRepository).deleteById(userId);
        }

        @Test
        @DisplayName("Should throw NotFoundException when user does not exist")
        void deleteUser_whenUserNotExists_thenThrowNotFoundException() {
            Long userId = random.nextLong(1L, 100L);

            when(userRepository.existsById(userId)).thenReturn(false);

            assertThatThrownBy(() -> userService.deleteUser(userId))
                    .isInstanceOf(NotFoundException.class);

            verify(userRepository).existsById(userId);
            verify(userRepository, never()).deleteById(any());
        }
    }

    private User getNewUser() {
        return User.builder()
                .id(random.nextLong(1L, 1000L))
                .name(UUID.randomUUID().toString().substring(0, 15))
                .email(UUID.randomUUID().toString().substring(0, 8) + "@test.com")
                .build();
    }
}