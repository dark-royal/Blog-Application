package org.example.infrastructure.adapters.input.rest.controllers;

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
public class CommentController {

    private final CommentOnPostUseCase commentOnPostUseCase;
    private final CommentRestMapper commentRestMapper;
    private final ViewAllPostCommentUseCase viewAllPostCommentUseCase;
    private final DeleteCommentUseCase deleteCommentUseCase;

    @PostMapping("/post/{postId}/comment")
    public ResponseEntity<CommentResponse> commentOnPost(
            @RequestBody @Valid CommentRequest commentRequest,
            @PathVariable("postId") Long postId,
            @AuthenticationPrincipal User user
    ) throws PostNotFoundException, UserNotFoundException {

        Comment comment = commentRestMapper.toComment(commentRequest);

        Comment savedComment = commentOnPostUseCase.writeComment(comment, user, postId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentRestMapper.toCommentResponse(savedComment));
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> viewAllPostComments(@PathVariable("postId") Long postId) throws PostNotFoundException {
        List<Comment> comments = viewAllPostCommentUseCase.viewAllPostCommentsByPostId(postId);

        List<CommentResponse> responseList = comments.stream()
                .map(commentRestMapper::toViewAllPostCommentResponse)
                .toList();

        return ResponseEntity.ok(responseList);
    }

    @DeleteMapping("")
    public ResponseEntity<DeleteCommentResponse> deleteComment(@RequestParam(name = "commentId")Long commentId, @RequestParam(name = "postId")
    Long postId) throws CommentNotFoundException {
        deleteCommentUseCase.deleteComment(postId,commentId);
        DeleteCommentResponse response = new DeleteCommentResponse();
        response.setMessage("Successfully deleted post");
        response.setDateDeleted(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}







