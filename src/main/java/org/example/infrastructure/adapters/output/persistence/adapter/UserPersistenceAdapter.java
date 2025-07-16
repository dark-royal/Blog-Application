package org.example.infrastructure.adapters.output.persistence.adapter;

import lombok.extern.slf4j.Slf4j;
import org.example.application.port.output.UserPersistenceOutputPort;
import org.example.domain.exceptions.UserNotFoundException;
import org.example.domain.models.User;
import org.example.infrastructure.adapters.input.rest.messages.ErrorMessages;
import org.example.infrastructure.adapters.output.persistence.mapper.UserPersistenceMapper;
import org.example.infrastructure.adapters.output.persistence.entity.UserEntity;
import org.example.infrastructure.adapters.output.persistence.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.example.domain.validator.InputValidator.validateInput;

@Service
@Slf4j
public class UserPersistenceAdapter implements UserPersistenceOutputPort {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserPersistenceMapper userPersistenceMapper;

    @Override
    public User saveUser(User user) {
        log.info("Saving user: {}", user);

        UserEntity entity = userPersistenceMapper.toEntity(user);
        log.info("Mapped to entity: {}", entity);

        entity = userRepository.save(entity);
        log.info("Saved entity: {}", entity);

        User savedUser = userPersistenceMapper.toModel(entity);
        log.info("Mapped back to domain user: {}", savedUser);

        return savedUser;
    }

    @Override
    public User getUserById(Long id) throws UserNotFoundException {
        UserEntity entity = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(ErrorMessages.USER_NOT_FOUND));
        return userPersistenceMapper.toModel(entity);

    }

//    @Override
//    public User getUserByEmail(String email) throws UserNotFoundException {
//        validateInput(email);
//        UserEntity entity = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(ErrorMessages.USER_NOT_FOUND));
//        return userPersistenceMapper.toModel(entity);
//    }

    public User getUserByEmail(String email) throws UserNotFoundException {
        validateInput(email);
        log.info("Searching for user with email: {}", email);
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found for email: {}", email);
                    return new UserNotFoundException(ErrorMessages.USER_NOT_FOUND);
                });
        log.info("Found user: {}", user.getId());
        return userPersistenceMapper.toModel(user);
    }

    @Override
    public boolean userExistsByEmail(String email) {
        validateInput(email);
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

}
