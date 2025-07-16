package org.example.infrastructure.adapters.config.generalAppConfig;

import org.example.application.port.output.IdentityManagementOutputPort;
import org.example.application.port.output.PostPersistenceOutputPort;
import org.example.domain.services.PostService;
import org.example.infrastructure.adapters.output.keycloak.KeycloakAdapter;
import org.keycloak.admin.client.Keycloak;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppBeans {

    @Bean
    public KeycloakAdapter keycloakAdapter(Keycloak keycloak) {
        return new KeycloakAdapter(keycloak);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();

    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public IdentityManagementOutputPort identityManagementOutputPort(Keycloak keycloak) {
        return new KeycloakAdapter(keycloak);
    }

    



//    @Bean
//    public PostPersistenceOutputPort postPersistenceOutputPort(){
//        return PostPersistenceAdapter
//    }
//
//    @Bean
//    public PostService postService(PostPersistenceOutputPort postPersistenceOutputPort){
//        return new PostService(postPersistenceOutputPort);
//    }
}
