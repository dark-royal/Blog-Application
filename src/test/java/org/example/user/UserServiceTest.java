package org.example.user;

import org.example.application.port.output.IdentityManagementOutputPort;
import org.example.application.port.output.UserPersistenceOutputPort;
import org.example.domain.exceptions.*;
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
        user.setEmail(input);
        user.setPassword(input);
        user.setFirstName(input);
        user.setLastName(input);
        user.setRole(input);

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
    void logout_Success() throws IdentityManagerException {
        User user = new User();
        user.setRefreshToken("valid-refresh-token");

        doNothing().when(identityManagementOutputPort).logoutUser(user);

        assertDoesNotThrow(() -> userService.logout(user));
        verify(identityManagementOutputPort, times(1)).logoutUser(user);
    }

    @Test
    void logout_NullRefreshToken_ThrowsException() throws IdentityManagerException {

        User user = new User();
        user.setRefreshToken(null);

        assertThrows(IllegalArgumentException.class, () -> userService.logout(user));
        verify(identityManagementOutputPort, never()).logoutUser(any());
    }

    @Test
    void logout_EmptyRefreshToken_ThrowsException() throws IdentityManagerException {
        User user = new User();
        user.setRefreshToken("");

        assertThrows(IllegalArgumentException.class, () -> userService.logout(user));
        verify(identityManagementOutputPort, never()).logoutUser(any());
    }

    @Test
    void logout_KeycloakFailure_ThrowsException() throws IdentityManagerException {
        User user = new User();
        user.setRefreshToken("valid-token");

        doThrow(new IdentityManagerException("Keycloak error"))
                .when(identityManagementOutputPort).logoutUser(user);

        assertThrows(IdentityManagerException.class, () -> userService.logout(user));
        verify(identityManagementOutputPort, times(1)).logoutUser(user);
    }



}
