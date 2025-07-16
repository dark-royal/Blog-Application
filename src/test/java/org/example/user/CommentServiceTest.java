package org.example.user;

import org.example.application.port.output.CommentPersistenceOutputPort;
import org.example.application.port.output.PostPersistenceOutputPort;
import org.example.application.port.output.UserPersistenceOutputPort;
import org.example.domain.exceptions.CommentNotFoundException;
import org.example.domain.exceptions.PostNotFoundException;
import org.example.domain.exceptions.UserNotFoundException;
import org.example.domain.models.Comment;
import org.example.domain.models.Post;
import org.example.domain.models.User;
import org.example.domain.services.CommentService;
import org.example.infrastructure.adapters.input.rest.messages.ErrorMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private UserPersistenceOutputPort userPersistenceOutputPort;

    @Mock
    private PostPersistenceOutputPort postPersistenceOutputPort;

    @Mock
    private CommentPersistenceOutputPort commentPersistenceOutputPort;

    @InjectMocks
    private CommentService commentService;

    private User user;
    private Post post;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        post = new Post();
        post.setId(10L);
        post.setTitle("Test Post");
    }

    @Test
    void writeComment_shouldSaveCommentSuccessfully() throws UserNotFoundException, PostNotFoundException, UserNotFoundException, PostNotFoundException {

        Comment comment = new Comment();
        comment.setContent("This is a comment");

        when(userPersistenceOutputPort.getUserById(user.getId())).thenReturn(user);
        when(postPersistenceOutputPort.getPostById(post.getId())).thenReturn(post);
        when(commentPersistenceOutputPort.saveComment(any(Comment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Comment savedComment = commentService.writeComment(comment, user, post.getId());

        assertEquals("This is a comment", savedComment.getContent());
        assertEquals(post, savedComment.getPost());
        assertEquals(user, savedComment.getUser());
        assertNotNull(savedComment.getCommentedAt());

        verify(userPersistenceOutputPort).getUserById(user.getId());
        verify(postPersistenceOutputPort).getPostById(post.getId());
        verify(commentPersistenceOutputPort).saveComment(any(Comment.class));
    }

    @Test
    void writeComment_shouldThrowUserNotFoundException_whenUserDoesNotExist() throws UserNotFoundException {

        Comment comment = new Comment();
        comment.setContent("Comment");

        when(userPersistenceOutputPort.getUserById(user.getId()))
                .thenThrow(new UserNotFoundException(ErrorMessages.USER_NOT_FOUND));

        assertThrows(UserNotFoundException.class,
                () -> commentService.writeComment(comment, user, post.getId()));

        verify(userPersistenceOutputPort).getUserById(user.getId());
        verifyNoInteractions(postPersistenceOutputPort, commentPersistenceOutputPort);
    }

    @Test
    void writeComment_shouldThrowPostNotFoundException_whenPostDoesNotExist() throws UserNotFoundException, PostNotFoundException {

        Comment comment = new Comment();
        comment.setContent("Comment");

        when(userPersistenceOutputPort.getUserById(user.getId())).thenReturn(user);
        when(postPersistenceOutputPort.getPostById(post.getId()))
                .thenThrow(new PostNotFoundException(ErrorMessages.POST_NOT_FOUND));

        assertThrows(PostNotFoundException.class,
                () -> commentService.writeComment(comment, user, post.getId()));

        verify(userPersistenceOutputPort).getUserById(user.getId());
        verify(postPersistenceOutputPort).getPostById(post.getId());
        verifyNoInteractions(commentPersistenceOutputPort);
    }

    static Stream<String> invalidInputs() {
        return Stream.of(null, "", " ");
    }


    @ParameterizedTest
    @MethodSource("invalidInputs")
    public void testInvalidContent_ThrowIllegalArgumentException(String input){
        Comment comment = new Comment();
        comment.setContent(input);
        assertThrows(IllegalArgumentException.class,()->commentService.writeComment(comment, user, post.getId()));

    }

    @Test
    void viewAllPostCommentsByPostId_shouldReturnListOfComments_whenPostExists() throws PostNotFoundException {
        Long postId = 1L;
        Post post = new Post();
        post.setId(postId);

        List<Comment> comments = List.of(new Comment(), new Comment());

        when(postPersistenceOutputPort.getPostById(postId)).thenReturn(post);
        when(commentPersistenceOutputPort.getAllCommentsByPostId(postId)).thenReturn(comments);

        List<Comment> result = commentService.viewAllPostCommentsByPostId(postId);

        assertEquals(2, result.size());
        verify(postPersistenceOutputPort).getPostById(postId);
        verify(commentPersistenceOutputPort).getAllCommentsByPostId(postId);
    }



    @Test
    void deleteComment_shouldDeleteComment_whenUserOwnsComment() throws CommentNotFoundException, AccessDeniedException, java.nio.file.AccessDeniedException {
        Long commentId = 5L;
        Long postId = 10L;

        User currentUser = new User();
        currentUser.setId(1L);

        Post post = new Post();
        post.setId(postId);
        post.setUser(new User());
        post.getUser().setId(99L);

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setPost(post);
        comment.setUser(currentUser);

        when(commentPersistenceOutputPort.getCommentByIdAndPostId(commentId, postId)).thenReturn(comment);

        commentService.deleteComment(commentId, postId, currentUser);

        verify(commentPersistenceOutputPort).getCommentByIdAndPostId(commentId, postId);
        verify(commentPersistenceOutputPort).deleteCommentById(commentId);
    }

    @Test
    void deleteComment_shouldDeleteComment_whenUserOwnsPost() throws CommentNotFoundException, AccessDeniedException, java.nio.file.AccessDeniedException {
        Long commentId = 5L;
        Long postId = 10L;


        User commentAuthor = new User();
        commentAuthor.setId(2L);

        User postOwner = new User();
        postOwner.setId(1L);

        Post post = new Post();
        post.setId(postId);
        post.setUser(postOwner);

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setPost(post);
        comment.setUser(commentAuthor);

        when(commentPersistenceOutputPort.getCommentByIdAndPostId(commentId, postId)).thenReturn(comment);

        commentService.deleteComment(commentId, postId, postOwner);

        verify(commentPersistenceOutputPort).getCommentByIdAndPostId(commentId, postId);
        verify(commentPersistenceOutputPort).deleteCommentById(commentId);
    }



    @Test
    void deleteComment_shouldThrowAccessDenied_whenUserIsNotOwner() throws CommentNotFoundException {
        Long commentId = 5L;
        Long postId = 10L;

        User currentUser = new User();
        currentUser.setId(1L); // Not the post or comment owner

        User commentOwner = new User();
        commentOwner.setId(2L);

        User postOwner = new User();
        postOwner.setId(3L); // Another different person

        Post post = new Post();
        post.setId(postId);
        post.setUser(postOwner); // Post is owned by someone else

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setPost(post);
        comment.setUser(commentOwner);

        when(commentPersistenceOutputPort.getCommentByIdAndPostId(commentId, postId)).thenReturn(comment);

        assertThrows(AccessDeniedException.class, () ->
                commentService.deleteComment(commentId, postId, currentUser)
        );

        verify(commentPersistenceOutputPort).getCommentByIdAndPostId(commentId, postId);
        verify(commentPersistenceOutputPort, never()).deleteCommentById(any());
    }




    @Test
    void deleteComment_shouldThrowCommentNotFound_whenCommentDoesNotExist() throws CommentNotFoundException {
        Long commentId = 5L;
        Long postId = 10L;

        User currentUser = new User();
        currentUser.setId(1L);

        when(commentPersistenceOutputPort.getCommentByIdAndPostId(commentId, postId))
                .thenThrow(new CommentNotFoundException("Comment not found"));

        assertThrows(CommentNotFoundException.class, () ->
                commentService.deleteComment(commentId, postId, currentUser)
        );

        verify(commentPersistenceOutputPort).getCommentByIdAndPostId(commentId, postId);
        verify(commentPersistenceOutputPort, never()).deleteCommentById(any());
    }








}
