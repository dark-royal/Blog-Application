package org.example.infrastructure.adapters.config.generalAppConfig;

import org.example.application.port.output.IdentityManagementOutputPort;
import org.example.infrastructure.adapters.output.keycloak.KeycloakAdapter;
import org.keycloak.admin.client.Keycloak;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppBeans {

    @Bean
    public KeycloakAdapter keycloakAdapter(Keycloak keycloak) {
        return new KeycloakAdapter(keycloak);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public IdentityManagementOutputPort identityManagementOutputPort(Keycloak keycloak) {
        return new KeycloakAdapter(keycloak);
    }
}
