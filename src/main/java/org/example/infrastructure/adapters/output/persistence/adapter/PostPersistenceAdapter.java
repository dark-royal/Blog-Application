package org.example.infrastructure.adapters.output.persistence.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.application.port.output.PostPersistenceOutputPort;
import org.example.domain.exceptions.PostNotFoundException;
import org.example.domain.models.Post;
import org.example.domain.models.User;
import org.example.infrastructure.adapters.input.rest.messages.ErrorMessages;
import org.example.infrastructure.adapters.output.persistence.entity.PostEntity;
import org.example.infrastructure.adapters.output.persistence.entity.UserEntity;
import org.example.infrastructure.adapters.output.persistence.mapper.PostPersistenceMapper;
import org.example.infrastructure.adapters.output.persistence.repositories.PostRepository;
import org.springframework.stereotype.Service;

import static org.example.domain.validator.InputValidator.validateInput;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostPersistenceAdapter implements PostPersistenceOutputPort {


    private final PostPersistenceMapper postPersistenceMapper;
    private final PostRepository postRepository;


    @Override
    public Post savePost(Post post) {

        log.info("Saving user: {}", post);

        PostEntity entity = postPersistenceMapper.toEntity(post);
        log.info("Mapped to entity: {}", entity);

        entity = postRepository.save(entity);
        log.info("Saved entity: {}", entity);

        Post savedPost = postPersistenceMapper.toPost(entity);
        log.info("Mapped back to domain user: {}", savedPost);

        return savedPost;
    }

    @Override
    public Post getByTitle(String title) throws PostNotFoundException {
        PostEntity post = postRepository.findByTitle(title).orElseThrow(() -> new PostNotFoundException(ErrorMessages.POST_NOT_FOUND));
        return postPersistenceMapper.toPost(post);
    }

    @Override
    public boolean existsByTitle(String title) {
        validateInput(title);

        return postRepository.existsByTitle(title);
    }


}
