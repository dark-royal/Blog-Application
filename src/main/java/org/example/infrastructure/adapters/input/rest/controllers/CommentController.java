package org.example.infrastructure.adapters.input.rest.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.application.port.input.CommentOnPostUseCase;
import org.example.domain.exceptions.PostNotFoundException;
import org.example.domain.exceptions.UserNotFoundException;
import org.example.domain.models.Comment;
import org.example.domain.models.User;
import org.example.infrastructure.adapters.input.rest.data.request.CommentRequest;
import org.example.infrastructure.adapters.input.rest.data.response.CommentResponse;
import org.example.infrastructure.adapters.input.rest.mapper.CommentRestMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentOnPostUseCase commentOnPostUseCase;
    private final CommentRestMapper commentRestMapper;

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
}
