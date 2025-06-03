package org.example.keycloak;

import lombok.RequiredArgsConstructor;
import org.example.application.port.output.IdentityManagementOutputPort;
import org.example.domain.exceptions.AuthenticationException;
import org.example.domain.exceptions.IdentityManagerException;
import org.example.domain.exceptions.UserAlreadyExistException;
import org.example.domain.exceptions.UserNotFoundException;
import org.example.domain.models.User;
import org.example.infrastructure.adapters.output.keycloak.KeycloakAdapter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KeycloakAdapterTest {

    @Autowired
    private IdentityManagementOutputPort identityManagementOutputPort;

    @Autowired
    private KeycloakAdapter keycloakAdapter;

    private User user;






    @Test
    public void testThatUserCanBeCreated() throws IdentityManagerException {
        try {
            user = new User();
            user.setId(100L);
            user.setUsername("simi");
            user.setPassword("password");
            user.setEmail("simi@gmail.com");
            user.setFirstName("loluwa");
            user.setLastName("ibro");
            user.setRole("user");

            identityManagementOutputPort.createUser(user);
        } catch (Exception | UserAlreadyExistException e) {
            throw new IdentityManagerException(e.getMessage());
        }

    }

    @Test
    public void testThatUserAlreadyExist_throwsUserAlreadyExistException() throws IdentityManagerException, UserAlreadyExistException {

        user = new User();
        user.setId(200L);
        user.setUsername("praise");
        user.setPassword("password");
        user.setEmail("praise@gmail.com");
        user.setFirstName("florence");
        user.setLastName("kemi");
        user.setRole("user");
        identityManagementOutputPort.createUser(user);
        assertThrows(UserAlreadyExistException.class, () -> identityManagementOutputPort.createUser(user));

    }

    @Test
    public void testThatUserCanLogin() throws AuthenticationException, UserAlreadyExistException, IdentityManagerException {
        user = new User();
        user.setId(200L);
        user.setUsername("oyewole");
        user.setPassword("password");
        user.setEmail("titi@gmail.com");
        user.setFirstName("lope");
        user.setLastName("kemi");
        user.setRole("user");
        identityManagementOutputPort.createUser(user);
        user.setEmail("titi@gmail.com");
        user.setPassword("password");
        identityManagementOutputPort.loginUser(user);
        assertNotNull(user);
        assertNotNull(user.getAccessToken());
        assertNotNull(user.getRefreshToken());
    }


    @Test
    public void testThatUserCantLoginWithInvalidCredentials() throws UserAlreadyExistException, IdentityManagerException {
        user = new User();
        user.setId(300L);
        user.setUsername("precious");
        user.setPassword("password");
        user.setEmail("precy@gmail.com");
        user.setFirstName("lope");
        user.setLastName("kemi");
        user.setRole("user");
        identityManagementOutputPort.createUser(user);
        user.setEmail("precy@gmail.com");
        user.setPassword("password12");
        assertThrows(AuthenticationException.class, () -> identityManagementOutputPort.loginUser(user));
    }



    @Test
    public void testThatUserCanLogout() throws Exception, UserAlreadyExistException, AuthenticationException, IdentityManagerException {
        user = new User();
        user.setId(400L);
        user.setUsername("tunde");
        user.setPassword("password");
        user.setEmail("tunde@gmail.com");
        user.setFirstName("tunde");
        user.setLastName("ade");
        user.setRole("user");
        identityManagementOutputPort.createUser(user);

        User loginAttempt = new User();
        loginAttempt.setEmail(user.getEmail());
        loginAttempt.setPassword("password");
        identityManagementOutputPort.loginUser(loginAttempt);

        assertNotNull(loginAttempt.getRefreshToken(), "Refresh token should not be null after login");

        keycloakAdapter.logoutUser(loginAttempt);

        assertNull(loginAttempt.getAccessToken(), "Access token should be null after logout");
        assertNull(loginAttempt.getRefreshToken(), "Refresh token should be null after logout");
        assertNull(loginAttempt.getScope(), "Scope should be null after logout");
        assertNull(loginAttempt.getTokenType(), "Token type should be null after logout");
    }

    @Test
    public void testThatLogoutWithInvalidRefreshToken_ThrowsIdentityManagerException() {
        User invalidUser = new User();
        invalidUser.setRefreshToken("this-is-an-invalid-token");
        assertThrows(IdentityManagerException.class, () -> keycloakAdapter.logoutUser(invalidUser));
    }

}
