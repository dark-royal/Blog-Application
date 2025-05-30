package org.example.user;

import org.example.application.port.output.CommentPersistenceOutputPort;
import org.example.application.port.output.PostPersistenceOutputPort;
import org.example.application.port.output.UserPersistenceOutputPort;
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


}
