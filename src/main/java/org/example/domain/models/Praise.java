package org.example.domain.models;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
@RequiredArgsConstructor
public class Praise {


    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public static void main(String[] args) {

        System.out.println(passwordEncoder.matches("similoluwa", "$2a$10$MTkpbK/w9fz00mIJHzq11OtP9fDKq3mEFBQsf5gNbo7v2Y/hh8As."));
    }
}
