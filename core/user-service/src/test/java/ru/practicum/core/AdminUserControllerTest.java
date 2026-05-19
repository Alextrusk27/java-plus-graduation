package ru.practicum.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.core.controller.AdminUserController;
import ru.practicum.core.dto.user.request.NewUserRequest;
import ru.practicum.core.dto.user.response.UserDto;
import ru.practicum.core.service.UserService;
import ru.practicum.core.utils.ApiPaths;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
@DisplayName("AdminUserController Unit Tests")
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Nested
    @DisplayName("GET /admin/users - Get Users")
    class GetUsersTests {

        @Test
        @DisplayName("Should return list of users when no filters provided")
        void getUsers_whenNoParams_thenReturnsList() throws Exception {
            List<UserDto> users = List.of(
                    new UserDto(1L, "User One", "user1@test.com"),
                    new UserDto(2L, "User Two", "user2@test.com")
            );

            when(userService.getUsers(any())).thenReturn(users);

            mockMvc.perform(get(ApiPaths.Admin.USERS))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].name").value("User One"))
                    .andExpect(jsonPath("$[1].id").value(2L))
                    .andExpect(jsonPath("$[1].name").value("User Two"));

            verify(userService, times(1)).getUsers(any());
        }

        @Test
        @DisplayName("Should return filtered users when IDs provided")
        void getUsers_whenWithIds_thenReturnsFilteredList() throws Exception {
            List<Long> ids = List.of(1L, 2L);
            List<UserDto> users = List.of(
                    new UserDto(1L, "User One", "user1@test.com"),
                    new UserDto(2L, "User Two", "user2@test.com")
            );

            when(userService.getUsers(any())).thenReturn(users);

            mockMvc.perform(get(ApiPaths.Admin.USERS)
                            .param("ids", "1", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));

            verify(userService, times(1)).getUsers(any());
        }

        @Test
        @DisplayName("Should apply pagination and return users")
        void getUsers_whenWithPagination_thenReturnsList() throws Exception {
            List<UserDto> users = List.of(new UserDto(1L, "User One", "user1@test.com"));

            when(userService.getUsers(any())).thenReturn(users);

            mockMvc.perform(get(ApiPaths.Admin.USERS)
                            .param("from", "5")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));

            verify(userService, times(1)).getUsers(any());
        }

        @Test
        @DisplayName("Should return empty list when IDs list is empty")
        void getUsers_whenIdsEmpty_thenReturnsEmptyList() throws Exception {
            when(userService.getUsers(any())).thenReturn(List.of());

            mockMvc.perform(get(ApiPaths.Admin.USERS)
                            .param("ids", ""))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(userService, times(1)).getUsers(any());
        }
    }

    @Nested
    @DisplayName("POST /admin/users - Create User")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user and return 201 with user data")
        void createUser_whenValidRequest_thenReturns201() throws Exception {
            NewUserRequest request = new NewUserRequest("Test User", "test@test.com");
            UserDto response = new UserDto(1L, "Test User", "test@test.com");

            when(userService.createUser(any(NewUserRequest.class))).thenReturn(response);

            mockMvc.perform(post(ApiPaths.Admin.USERS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("Test User"))
                    .andExpect(jsonPath("$.email").value("test@test.com"));

            verify(userService, times(1)).createUser(any(NewUserRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when email is missing")
        void createUser_whenEmailIsNull_thenReturns400() throws Exception {
            String invalidJson = """
                    {
                        "name": "Test User"
                    }
                    """;

            mockMvc.perform(post(ApiPaths.Admin.USERS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).createUser(any());
        }

        @Test
        @DisplayName("Should return 400 when email is blank")
        void createUser_whenEmailIsBlank_thenReturns400() throws Exception {
            String invalidJson = """
                    {
                        "email": "",
                        "name": "Test User"
                    }
                    """;

            mockMvc.perform(post(ApiPaths.Admin.USERS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).createUser(any());
        }

        @Test
        @DisplayName("Should return 400 when email format is invalid")
        void createUser_whenEmailInvalid_thenReturns400() throws Exception {
            String invalidJson = """
                    {
                        "email": "invalid-email",
                        "name": "Test User"
                    }
                    """;

            mockMvc.perform(post(ApiPaths.Admin.USERS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).createUser(any());
        }

        @Test
        @DisplayName("Should return 400 when name is missing")
        void createUser_whenNameIsNull_thenReturns400() throws Exception {
            String invalidJson = """
                    {
                        "email": "test@test.com"
                    }
                    """;

            mockMvc.perform(post(ApiPaths.Admin.USERS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).createUser(any());
        }
    }

    @Nested
    @DisplayName("DELETE /admin/users/{id} - Delete User")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user and return 204 No Content")
        void deleteUser_whenUserExists_thenReturns204() throws Exception {
            doNothing().when(userService).deleteUser(1L);

            mockMvc.perform(delete(ApiPaths.Admin.USERS + "/1"))
                    .andExpect(status().isNoContent());

            verify(userService, times(1)).deleteUser(1L);
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        void deleteUser_whenUserNotExists_thenReturns404() throws Exception {
            doThrow(new ru.practicum.core.exception.NotFoundException("User not found"))
                    .when(userService).deleteUser(999L);

            mockMvc.perform(delete(ApiPaths.Admin.USERS + "/999"))
                    .andExpect(status().isNotFound());

            verify(userService, times(1)).deleteUser(999L);
        }

        @Test
        @DisplayName("Should return 400 when userId is not a number")
        void deleteUser_whenInvalidUserId_thenReturns400() throws Exception {
            mockMvc.perform(delete(ApiPaths.Admin.USERS + "/abc"))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).deleteUser(any());
        }
    }
}