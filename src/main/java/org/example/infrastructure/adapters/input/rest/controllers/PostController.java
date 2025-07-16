package org.example.infrastructure.adapters.input.rest.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Posts", description = "Operations related to post management")
public class PostController {

    private final CreatePostUseCase createPostUseCase;
    private final DeletePostUseCase deletePostUseCase;
    private final EditPostUseCase editPostUseCase;
    private final PostRestMapper postRestMapper;
    private final ViewPostUseCase viewPostUseCase;
    private final ViewAllPostUseCase viewAllPostUseCase;

    @Operation(summary = "Create a post", description = "Allows authenticated users to create a new post")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Post created successfully", content = @Content(schema = @Schema(implementation = CreatePostResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid post data"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "Post already exist")
    })
    @SecurityRequirement(name = "Keycloak")
    @PostMapping("/post")
    public ResponseEntity<CreatePostResponse> createPost(@AuthenticationPrincipal User user,
                                                         @Valid @RequestBody CreatePostRequest createPostRequest)
            throws UserNotFoundException, PostAlreadyExistsException {
        Post post = postRestMapper.toPost(createPostRequest);
        Post createdPost = createPostUseCase.createPost(user, post);
        createdPost.setPublishedDate(LocalDateTime.now());

        CreatePostResponse response = postRestMapper.toCreatePostResponse(createdPost);
        response.setPublishedDate(createdPost.getPublishedDate());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Delete a post", description = "Deletes a post by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @SecurityRequirement(name = "Keycloak")
    @DeleteMapping("/{postId}")
    public ResponseEntity<DeletePostResponse> deletePost(@AuthenticationPrincipal User user,
                                                         @PathVariable("postId") Long postId)
            throws UserNotFoundException, AccessDeniedException, PostNotFoundException {
        deletePostUseCase.deletePost(user, postId);

        DeletePostResponse response = new DeletePostResponse();
        response.setMessage("Successfully deleted post");
        response.setDateDeleted(LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Edit a post", description = "Updates a post by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Post updated successfully", content = @Content(schema = @Schema(implementation = EditPostResponse.class))),
            @ApiResponse(responseCode = "404", description = "Post not found"),
            @ApiResponse(responseCode = "409", description = "fields are empty")
    })
    @SecurityRequirement(name = "Keycloak")
    @PutMapping("/{id}")
    public ResponseEntity<EditPostResponse> editPost(@AuthenticationPrincipal User user,
                                                     @PathVariable("id") Long id,
                                                     @Valid @RequestBody EditPostRequest editPostRequest)
            throws UserNotFoundException, PostAlreadyExistsException, AccessDeniedException, PostNotFoundException {
        Post post = postRestMapper.toPost(editPostRequest);
        post.setId(id);
        Post editedPost = editPostUseCase.editPost(user, post);
        editedPost.setUpdatedDate(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.CREATED).body(postRestMapper.toEditPostResponse(editedPost));
    }

    @Operation(summary = "Get post by ID", description = "Fetch a single post")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post retrieved", content = @Content(schema = @Schema(implementation = ViewPostResponse.class))),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @GetMapping("/{postId}")
    public ResponseEntity<ViewPostResponse> viewPost(@PathVariable("postId") Long postId)
            throws PostNotFoundException {
        Post post = viewPostUseCase.viewPost(postId);

        ViewPostResponse response = new ViewPostResponse();
        response.setId(post.getId());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setCreatedAt(post.getPublishedDate());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all posts by user", description = "Fetch all posts created by a specific user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Posts retrieved", content = @Content(schema = @Schema(implementation = ViewAllUserPostResponse.class))),
            @ApiResponse(responseCode = "404", description = "User or posts not found")
    })
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


//@Override
//public void changePassword(String email, String oldPassword, String newPassword) {
//    // Step 1: Validate input
//    validateEmail(email);
//    validatePassword(oldPassword);
//    validatePassword(newPassword);
//
//    // Step 2: Fetch user from DB
//    User user = userOutputPort.getUserByEmail(email);
//
//    // Step 3: Match old password
//    if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
//        throw new IdentityManagementException("Old password is incorrect", HttpStatus.UNAUTHORIZED);
//    }
//
//    // Step 4: Encode and set new password
//    String encodedNewPassword = passwordEncoder.encode(newPassword);
//    user.setNewPassword(encodedNewPassword); // For Keycloak
//    user.setPassword(encodedNewPassword);    // For your database
//
//    // Step 5: Update password in Keycloak
//    identityManagementOutputPort.changePassword(user);
//
//    // Step 6: Save updated password in local DB
//    userOutputPort.saveUser(user);
//}


//@Test
//    void testChangePasswordSuccess() {
//        when(userOutputPort.getUserByEmail(email)).thenReturn(user);
//        when(passwordEncoder.matches(oldPassword, encodedOldPassword)).thenReturn(true);
//        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);
//        when(userOutputPort.saveUser(any(User.class))).thenReturn(user);
//
//        assertDoesNotThrow(() -> userService.changePassword(email, oldPassword, newPassword));
//
//        verify(identityManagementOutputPort).changePassword(user);
//        verify(userOutputPort).saveUser(user);
//    }
//
//    @Test
//    void testChangePasswordFails_InvalidOldPassword() {
//        when(userOutputPort.getUserByEmail(email)).thenReturn(user);
//        when(passwordEncoder.matches(oldPassword, encodedOldPassword)).thenReturn(false);
//
//        IdentityManagementException exception = assertThrows(
//                IdentityManagementException.class,
//                () -> userService.changePassword(email, oldPassword, newPassword)
//        );
//
//        assertEquals("Old password is incorrect", exception.getMessage());
//        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
//
//        verify(identityManagementOutputPort, never()).changePassword(any());
//        verify(userOutputPort, never()).saveUser(any());
//    }
//
//    @Test
//    void testChangePasswordFails_InvalidEmail() {
//        IllegalArgumentException exception = assertThrows(
//                IllegalArgumentException.class,
//                () -> userService.changePassword(" ", oldPassword, newPassword)
//        );
//        assertEquals("Email must not be empty", exception.getMessage());
//    }
//
//    @Test
//    void testChangePasswordFails_EmptyNewPassword() {
//        IllegalArgumentException exception = assertThrows(
//                IllegalArgumentException.class,
//                () -> userService.changePassword(email, oldPassword, " ")
//        );
//        assertEquals("Password must not be empty", exception.getMessage());
//    }






