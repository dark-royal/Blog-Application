package org.example.user;

import org.example.application.port.output.IdentityManagementOutputPort;
import org.example.application.port.output.UserPersistenceOutputPort;
import org.example.domain.exceptions.AuthenticationException;
import org.example.domain.exceptions.InvalidCredentialsException;
import org.example.domain.exceptions.UserAlreadyExistException;
import org.example.domain.exceptions.UserNotFoundException;
import org.example.domain.models.User;
import org.example.domain.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
public class UserServiceTest {

    @Mock
    private IdentityManagementOutputPort identityManagementOutputPort;

    @Mock
    private UserPersistenceOutputPort userPersistenceOutputPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testThatUserCanSignUp() throws Exception, UserAlreadyExistException {
        User user = new User();
        user.setUsername("admin");
        user.setPassword("admin");
        user.setEmail("admin@example.com");
        user.setFirstName("admin");
        user.setLastName("admin");
        user.setRole("user");

        when(userPersistenceOutputPort.userExistsByEmail(user.getEmail())).thenReturn(false);
        when(identityManagementOutputPort.createUser(any())).thenReturn(user);
        when(userPersistenceOutputPort.saveUser(any())).thenReturn(user);

        User createdUser = userService.signUp(user);

        assertNotNull(createdUser);
        assertEquals(user.getEmail(), createdUser.getEmail());
        verify(identityManagementOutputPort, times(1)).createUser(any());
    }


    @Test
    public void testThatUserCannotRegisterTwice_throwUserAlreadyExistException() throws Exception, UserAlreadyExistException {
        User user = new User();
        user.setUsername("ogo");
        user.setPassword("admin");
        user.setEmail("ogo@example.com");
        user.setFirstName("ogooluwa");
        user.setLastName("afisiru");
        user.setRole("user");

        when(userPersistenceOutputPort.userExistsByEmail(user.getEmail())).thenReturn(true);

        assertThrows(UserAlreadyExistException.class, () -> {
            userService.signUp(user);
        });

        verify(identityManagementOutputPort, never()).createUser(any());
        verify(userPersistenceOutputPort, never()).saveUser(any());
    }

    @ParameterizedTest
    @MethodSource("invalidInputs")
    public void testInvalidEmail_EmptyAndBlankEmail_ThrowIllegalArgumentException(String input) throws Exception {

        User user = new User();
        user.setEmail("");
        user.setPassword("admin");
        user.setFirstName("admin");
        user.setLastName("admin");
        user.setRole("user");

        assertThrows(IllegalArgumentException.class, () -> userService.signUp(user));
    }


    static Stream<String> invalidInputs() {
        return Stream.of(null, "", " ");
    }

    @Test
    public void testThatUserCanLogin_success() throws UserNotFoundException, InvalidCredentialsException, AuthenticationException {

        User storedUser = new User();
        storedUser.setUsername("testuser");
        storedUser.setPassword("Password");
        storedUser.setEmail("test@example.com");
        storedUser.setFirstName("Test");
        storedUser.setLastName("User");
        storedUser.setRole("user");
        storedUser.setEnabled(true);

        User loginRequest = new User();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("Password");

        when(userPersistenceOutputPort.getUserByEmail("test@example.com")).thenReturn(storedUser);
//        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        User loggedInUser = userService.login(loginRequest);

        assertNotNull(loggedInUser);
        assertEquals("test@example.com", loggedInUser.getEmail());
        assertTrue(loggedInUser.isEnabled());
//        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }

    @Test
    public void testLoginWithInvalidEmail_throwUserNotFoundException() throws Exception, UserAlreadyExistException, UserNotFoundException {
        User user = new User();
        user.setUsername("admin");
        user.setPassword("admin");
        user.setEmail("admin@example.com");
        user.setFirstName("admin");
        user.setLastName("admin");
        user.setRole("user");


        when(userPersistenceOutputPort.getUserByEmail("admin@example.com")).thenReturn(user);
        when(userPersistenceOutputPort.getUserByEmail("jesu@gmail.com")).thenReturn(null);
        when(identityManagementOutputPort.createUser(any())).thenReturn(user);
        when(userPersistenceOutputPort.saveUser(any())).thenReturn(user);

        User createdUser = userService.signUp(user);
        assertNotNull(createdUser);
        assertEquals(user.getEmail(), createdUser.getEmail());
        verify(identityManagementOutputPort, times(1)).createUser(any());

        createdUser.setEmail("jesu@gmail.com");
        assertThrows(UserNotFoundException.class, () -> userService.login(createdUser));
    }

    @Test
    public void testLoginWithInvalidPassword_throwInvalidCredentialsException() throws Exception, UserAlreadyExistException, UserNotFoundException {
        User originalUser = new User();
        originalUser.setUsername("admin");
        originalUser.setPassword("admin");
        originalUser.setEmail("admin@example.com");
        originalUser.setFirstName("admin");
        originalUser.setLastName("admin");
        originalUser.setRole("user");
        originalUser.setEnabled(true);

        User loginUser = new User();
        loginUser.setEmail("admin@example.com");
        loginUser.setPassword("wrong-password");

        when(userPersistenceOutputPort.getUserByEmail("admin@example.com")).thenReturn(originalUser);

        assertThrows(InvalidCredentialsException.class, () -> userService.login(loginUser));
    }


    @Test
    void resetPassword_Success() throws Exception, UserNotFoundException, AuthenticationException {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("newPassword123");

        User existingUser = new User();
        existingUser.setEmail(user.getEmail());

        when(userPersistenceOutputPort.getUserByEmail(user.getEmail())).thenReturn(existingUser);
        when(identityManagementOutputPort.resetPassword(user)).thenReturn(user);

        User result = userService.resetPassword(user);

        assertThat(result).isEqualTo(user);
        verify(identityManagementOutputPort).resetPassword(user);

    }

    @Test
    void resetPassword_UserNotFound() throws UserNotFoundException {
        User user = new User();
        user.setEmail("nonexistent@example.com");
        user.setPassword("newPassword123");

        when(userPersistenceOutputPort.getUserByEmail(user.getEmail())).thenReturn(null);

       assertThrows(UserNotFoundException.class, () -> userService.resetPassword(user));
    }



    @ParameterizedTest
    @MethodSource("invalidInputs")
    void resetPassword_InvalidPassword(String input) throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword(input);
        assertThrows(IllegalArgumentException.class, () -> userService.resetPassword(user));


    }




}
