package org.example.infrastructure.adapters.config.keycloak;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@Setter
@Getter

public class KeyCloakConfig {

    @Value("${app.keycloak.admin.clientId}")
    private String clientId;

    @Value("${app.keycloak.realm}")
    private String realm;

    @Value("${app.keycloak.serverUrl}")
    private String serverUrl;

    @Value("${app.keycloak.admin.clientSecret}")
    private String clientSecret;

    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .clientSecret(clientSecret)
                .clientId(clientId)
                .grantType("client_credentials")
                .realm(realm)
                .serverUrl(serverUrl)
                .build();
    }
}
