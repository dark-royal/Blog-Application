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
import org.example.application.port.input.CommentOnPostUseCase;
import org.example.application.port.input.DeleteCommentUseCase;
import org.example.application.port.input.ViewAllPostCommentUseCase;
import org.example.domain.exceptions.CommentNotFoundException;
import org.example.domain.exceptions.PostNotFoundException;
import org.example.domain.exceptions.UserNotFoundException;
import org.example.domain.models.Comment;
import org.example.domain.models.Post;
import org.example.domain.models.User;
import org.example.infrastructure.adapters.input.rest.data.request.CommentRequest;
import org.example.infrastructure.adapters.input.rest.data.response.CommentResponse;
import org.example.infrastructure.adapters.input.rest.data.response.DeleteCommentResponse;
import org.example.infrastructure.adapters.input.rest.data.response.DeletePostResponse;
import org.example.infrastructure.adapters.input.rest.data.response.ViewAllUserPostResponse;
import org.example.infrastructure.adapters.input.rest.mapper.CommentRestMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Operations related to commenting on posts")
public class CommentController {

    private final CommentOnPostUseCase commentOnPostUseCase;
    private final CommentRestMapper commentRestMapper;
    private final ViewAllPostCommentUseCase viewAllPostCommentUseCase;
    private final DeleteCommentUseCase deleteCommentUseCase;

    @Operation(summary = "Add a comment to a post", description = "Creates a comment on a specific post")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Comment added", content = @Content(schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Post or user not found")
    })
    @SecurityRequirement(name = "Keycloak")
    @PostMapping("/post/{postId}/comment")
    public ResponseEntity<CommentResponse> commentOnPost(
            @Valid @RequestBody CommentRequest commentRequest,
            @PathVariable("postId") Long postId,
            @AuthenticationPrincipal User user
    ) throws PostNotFoundException, UserNotFoundException {
        Comment comment = commentRestMapper.toComment(commentRequest);
        Comment savedComment = commentOnPostUseCase.writeComment(comment, user, postId);

        return ResponseEntity.status(HttpStatus.CREATED).body(commentRestMapper.toCommentResponse(savedComment));
    }

    @Operation(summary = "View all comments on a post", description = "Retrieves all comments for a specific post")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comments retrieved", content = @Content(schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> viewAllPostComments(@PathVariable("postId") Long postId)
            throws PostNotFoundException {
        List<Comment> comments = viewAllPostCommentUseCase.viewAllPostCommentsByPostId(postId);
        List<CommentResponse> responseList = comments.stream()
                .map(commentRestMapper::toViewAllPostCommentResponse)
                .toList();
        return ResponseEntity.ok(responseList);
    }



    @Operation(summary = "Delete a comment", description = "Deletes a comment from a post")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment deleted", content = @Content(schema = @Schema(implementation = DeleteCommentResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @SecurityRequirement(name = "Keycloak")
    @DeleteMapping
    public ResponseEntity<DeleteCommentResponse> deleteComment(@RequestParam ("commentId")Long commentId,
                                                               @RequestParam("postId") Long postId,
                                                               @AuthenticationPrincipal User user) throws CommentNotFoundException, AccessDeniedException {

        deleteCommentUseCase.deleteComment(commentId, postId, user);

        DeleteCommentResponse response = new DeleteCommentResponse();
        response.setMessage("Successfully deleted comment");
        response.setDateDeleted(LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

}









