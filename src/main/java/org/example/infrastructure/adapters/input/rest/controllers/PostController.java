package org.example.infrastructure.adapters.input.rest.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.application.port.input.CreatePostUseCase;
import org.example.domain.exceptions.PostAlreadyExistsException;
import org.example.domain.exceptions.UserNotFoundException;
import org.example.domain.models.Post;
import org.example.domain.models.User;
import org.example.infrastructure.adapters.input.rest.data.request.CreatePostRequest;
import org.example.infrastructure.adapters.input.rest.data.response.CreatePostResponse;
import org.example.infrastructure.adapters.input.rest.mapper.PostRestMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController {

    private final CreatePostUseCase createPostUseCase;

    private final PostRestMapper postRestMapper;

    @PostMapping("/post")
    public ResponseEntity<CreatePostResponse> createPost(@AuthenticationPrincipal User user, @RequestBody @Valid CreatePostRequest createPostRequest) throws UserNotFoundException, PostAlreadyExistsException {

        Post post = postRestMapper.toPost(createPostRequest);

        Post createdPost = createPostUseCase.createPost(user,post);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(postRestMapper
                        .toCreatePostResponse(createdPost));

    }
}
