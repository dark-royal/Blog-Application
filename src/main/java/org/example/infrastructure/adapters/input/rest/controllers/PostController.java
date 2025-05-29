package org.example.infrastructure.adapters.input.rest.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.application.port.input.*;
import org.example.domain.exceptions.PostAlreadyExistsException;
import org.example.domain.exceptions.PostNotFoundException;
import org.example.domain.exceptions.UserNotFoundException;
import org.example.domain.models.Post;
import org.example.domain.models.User;
import org.example.infrastructure.adapters.input.rest.data.request.CreatePostRequest;
import org.example.infrastructure.adapters.input.rest.data.request.EditPostRequest;
import org.example.infrastructure.adapters.input.rest.data.response.*;
import org.example.infrastructure.adapters.input.rest.mapper.PostRestMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController {

    private final CreatePostUseCase createPostUseCase;

    private final DeletePostUseCase deletePostUseCase;

    private final EditPostUseCase editPostUseCase;

    private final PostRestMapper postRestMapper;

    private final ViewPostUseCase viewPostUseCase;
    private final ViewAllPostUseCase viewAllPostUseCase;

    @PostMapping("/post")
    public ResponseEntity<CreatePostResponse> createPost(@AuthenticationPrincipal User user, @RequestBody @Valid CreatePostRequest createPostRequest) throws UserNotFoundException, PostAlreadyExistsException {

        Post post = postRestMapper.toPost(createPostRequest);

        Post createdPost = createPostUseCase.createPost(user,post);
        createdPost.setPublishedDate(LocalDateTime.now());

        CreatePostResponse response = postRestMapper.toCreatePostResponse(createdPost);
        response.setPublishedDate(createdPost.getPublishedDate());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);

    }


    @DeleteMapping("/{postId}")
    public ResponseEntity<DeletePostResponse> deletePost(@AuthenticationPrincipal User user,  @PathVariable("postId")
    Long postId) throws UserNotFoundException, AccessDeniedException, PostNotFoundException {
        deletePostUseCase.deletePost(user,postId);
        DeletePostResponse response = new DeletePostResponse();
        response.setMessage("Successfully deleted post");
        response.setDateDeleted(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }




    @PutMapping("/{id}")
    public ResponseEntity<EditPostResponse> editPost(@AuthenticationPrincipal User user, @PathVariable("id") Long id, @RequestBody @Valid EditPostRequest editPostRequest) throws UserNotFoundException, PostAlreadyExistsException, AccessDeniedException, PostNotFoundException {

        Post post = postRestMapper.toPost(editPostRequest);

        post.setId(id);

        Post editedPost = editPostUseCase.editPost(user,post);
        editedPost.setUpdatedDate(LocalDateTime.now());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(postRestMapper
                        .toEditPostResponse(editedPost));

    }



    @GetMapping("/{postId}")
    public ResponseEntity<ViewPostResponse> viewPost(@PathVariable("postId")
    Long postId) throws PostNotFoundException {
         Post post = viewPostUseCase.viewPost(postId);

        ViewPostResponse response = new ViewPostResponse();
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setCreatedAt(post.getPublishedDate());
        response.setId(post.getId());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<ViewAllUserPostResponse>> viewAllUserPost(@PathVariable("id") Long id)
            throws UserNotFoundException, PostNotFoundException {

        List<Post> posts = viewAllPostUseCase.getAllPostsByUserId(id);

        List<ViewAllUserPostResponse> responseList = posts.stream()
                .map(postRestMapper::toViewAllUserPostResponse)
                .toList();

        return ResponseEntity.ok(responseList);
    }







}
