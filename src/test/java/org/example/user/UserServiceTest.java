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

        String encodedPassword = "encodedAdminPassword";

        when(userPersistenceOutputPort.userExistsByEmail(user.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("admin")).thenReturn(encodedPassword);
        when(identityManagementOutputPort.createUser(any())).thenAnswer(invocation -> {
            User createdUser = invocation.getArgument(0);
            createdUser.setPassword(encodedPassword);
            return createdUser;
        });
        when(userPersistenceOutputPort.saveUser(any())).thenAnswer(invocation -> invocation.getArgument(0));

        User createdUser = userService.signUp(user);

        assertNotNull(createdUser);
        assertEquals(user.getEmail(), createdUser.getEmail());
        assertEquals(encodedPassword, createdUser.getPassword());
        verify(identityManagementOutputPort, times(1)).createUser(any());
        verify(passwordEncoder, times(1)).encode("admin");
        verify(userPersistenceOutputPort, times(1)).saveUser(any());
    }

    @Test
    public void testThatUserCannotRegisterTwice_throwUserAlreadyExistException() throws UserAlreadyExistException, IdentityManagerException {
        User user = new User();
        user.setUsername("ogo");
        user.setPassword("admin");
        user.setEmail("ogo@example.com");
        user.setFirstName("ogooluwa");
        user.setLastName("afisiru");
        user.setRole("user");

        when(userPersistenceOutputPort.userExistsByEmail(user.getEmail())).thenReturn(true);
        assertThrows(UserAlreadyExistException.class, () -> userService.signUp(user));
        verify(identityManagementOutputPort, never()).createUser(any());
        verify(userPersistenceOutputPort, never()).saveUser(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    public void testPasswordIsEncodedBeforeSaving() throws Exception, UserAlreadyExistException {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("rawPassword123");
        user.setEmail("test@example.com");
        user.setFirstName("First");
        user.setLastName("Last");
        user.setRole("user");

        String encodedPassword = "encodedPassword123";

        when(userPersistenceOutputPort.userExistsByEmail(user.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("rawPassword123")).thenReturn(encodedPassword);
        when(identityManagementOutputPort.createUser(any())).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setPassword(encodedPassword);
            return u;
        });
        when(userPersistenceOutputPort.saveUser(any())).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.signUp(user);

        verify(passwordEncoder).encode("rawPassword123");
        assertEquals(encodedPassword, result.getPassword());
    }

    @ParameterizedTest
    @MethodSource("invalidInputs")
    public void testInvalidEmail_EmptyAndBlankEmail_ThrowIllegalArgumentException(String input) {
        User user = new User();
        user.setEmail(input);
        user.setPassword("password");
        user.setFirstName("first");
        user.setLastName("last");
        user.setRole("user");
        user.setUsername("username");

        assertThrows(IllegalArgumentException.class, () -> userService.signUp(user));
    }

    static Stream<String> invalidInputs() {
        return Stream.of(null, "", " ");
    }

    @Test
    public void testThatUserCanLogin_success() throws Exception, UserNotFoundException, InvalidCredentialsException, AuthenticationException {
        String rawPassword = "Password";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        User storedUser = new User();
        storedUser.setUsername("testuser");
        storedUser.setPassword(encodedPassword);
        storedUser.setEmail("test@example.com");
        storedUser.setEnabled(true);

        User loginRequest = new User();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword(rawPassword);

        when(userPersistenceOutputPort.getUserByEmail("test@example.com")).thenReturn(storedUser);
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        User loggedInUser = userService.login(loginRequest);
        assertNotNull(loggedInUser);
        assertEquals("test@example.com", loggedInUser.getEmail());
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }

    @Test
    public void testLoginWithInvalidEmail_throwUserNotFoundException() throws UserNotFoundException {
        User loginRequest = new User();
        loginRequest.setEmail("nonexistent@example.com");
        loginRequest.setPassword("password");

        when(userPersistenceOutputPort.getUserByEmail("nonexistent@example.com")).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> userService.login(loginRequest));
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    public void testLoginWithInvalidPassword_throwInvalidCredentialsException() throws UserNotFoundException {

        String encodedPassword = passwordEncoder.encode("correctPassword");

        User storedUser = new User();
        storedUser.setEmail("admin@example.com");
        storedUser.setPassword(encodedPassword);
        storedUser.setEnabled(true);

        User loginRequest = new User();
        loginRequest.setEmail("admin@example.com");
        loginRequest.setPassword("wrong-password");

        when(userPersistenceOutputPort.getUserByEmail("admin@example.com")).thenReturn(storedUser);
        when(passwordEncoder.matches("wrong-password", encodedPassword)).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> userService.login(loginRequest));
        verify(passwordEncoder).matches("wrong-password", encodedPassword);
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
