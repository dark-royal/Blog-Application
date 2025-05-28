package org.example.user;

import org.example.application.port.output.PostPersistenceOutputPort;
import org.example.application.port.output.UserPersistenceOutputPort;
import org.example.domain.exceptions.PostAlreadyExistsException;
import org.example.domain.exceptions.PostNotFoundException;
import org.example.domain.exceptions.UserNotFoundException;
import org.example.domain.models.Post;
import org.example.domain.models.User;
import org.example.domain.services.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private UserPersistenceOutputPort userPersistenceOutputPort;
    @Mock
    private PostPersistenceOutputPort postPersistenceOutputPort;


    private  User user;
    private Post existingPost;


    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(100L);
        user.setUsername("admin");
        user.setEmail("admin@example.com");

        existingPost = new Post();
        existingPost.setId(10L);
        existingPost.setTitle("Old Title");
        existingPost.setContent("Old Content");
        existingPost.setUser(user);


    }


    @Test
    public void testThatPostCanBeCreated() throws PostAlreadyExistsException, UserNotFoundException {
        Post post = new Post();
        post.setTitle("My Blog Application");
        post.setContent("I love to create content");

        when(userPersistenceOutputPort.existsById(user.getId())).thenReturn(true);
        when(postPersistenceOutputPort.existsByTitleAndUserId(post.getTitle(), user.getId())).thenReturn(false);

        Post savedPost = new Post();
        savedPost.setId(200L);
        savedPost.setTitle(post.getTitle());
        savedPost.setContent(post.getContent());
        savedPost.setUser(user);
        savedPost.setPublishedDate(LocalDateTime.now());

        when(postPersistenceOutputPort.savePost(any(Post.class))).thenReturn(savedPost);

        Post result = postService.createPost(user, post);

        assertNotNull(result);
        assertEquals(post.getTitle(), result.getTitle());
        assertEquals(post.getContent(), result.getContent());
        assertEquals(user, result.getUser());

        verify(userPersistenceOutputPort).existsById(user.getId());
        verify(postPersistenceOutputPort).existsByTitleAndUserId(post.getTitle(), user.getId());
        verify(postPersistenceOutputPort).savePost(any(Post.class));
    }

    @Test
    public void testCreatePost_ShouldThrow_WhenUserNotFound() {
        Post post = new Post();
        post.setTitle("Test Title");
        post.setContent("Test Content");
        User invalidUser = new User();
        invalidUser.setId(999L);

        when(userPersistenceOutputPort.existsById(invalidUser.getId())).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> {
            postService.createPost(invalidUser, post);
        });

        verify(userPersistenceOutputPort).existsById(invalidUser.getId());
        verifyNoInteractions(postPersistenceOutputPort);
    }

    @Test
    public void testCreatePost_ShouldThrow_WhenPostAlreadyExists() throws UserNotFoundException {
        Post post = new Post();
        post.setTitle("Existing Title");
        post.setContent("Existing Content");

        when(userPersistenceOutputPort.existsById(user.getId())).thenReturn(true);
        when(postPersistenceOutputPort.existsByTitleAndUserId(post.getTitle(), user.getId())).thenReturn(true);

        assertThrows(PostAlreadyExistsException.class, () -> {
            postService.createPost(user, post);
        });

        verify(userPersistenceOutputPort).existsById(user.getId());
        verify(postPersistenceOutputPort).existsByTitleAndUserId(post.getTitle(), user.getId());
        verify(postPersistenceOutputPort, never()).savePost(any());
    }



    static Stream<String> invalidInputs() {
        return Stream.of(null, "", " ");
    }


    @ParameterizedTest
    @MethodSource("invalidInputs")
    public void testInvalidTitleThrowsException(String title){
        Post post = new Post();
        post.setTitle(title);
        assertThrows(IllegalArgumentException.class, () -> postService.createPost(user, post));
    }

    @ParameterizedTest
    @MethodSource("invalidInputs")
    public void testInvalidContentThrowsException(String content){
        Post post = new Post();
        post.setContent(content);
        assertThrows(IllegalArgumentException.class, () -> postService.createPost(user, post));
    }


    @Test
    public void testThatPostCanBeDeleted() throws UserNotFoundException, PostAlreadyExistsException, AccessDeniedException, PostNotFoundException {
        Post post = new Post();
        post.setId(200L);
        post.setTitle("My Blog Application");
        post.setContent("I love to create content");

        Post postFromDb = new Post();
        postFromDb.setId(post.getId());
        postFromDb.setTitle(post.getTitle());
        postFromDb.setContent(post.getContent());
        postFromDb.setUser(user);
        postFromDb.setPublishedDate(LocalDateTime.now());

        when(userPersistenceOutputPort.existsById(user.getId())).thenReturn(true);
        when(postPersistenceOutputPort.getPostById(post.getId())).thenReturn(postFromDb);
        when(postPersistenceOutputPort.savePost(any(Post.class))).thenReturn(postFromDb);


        Post result = postService.createPost(user, post);

        assertNotNull(result);
        assertEquals(post.getTitle(), result.getTitle());
        assertEquals(post.getContent(), result.getContent());
        assertEquals(user, result.getUser());

        postService.deletePost(user, post.getId());
        verify(postPersistenceOutputPort).deletePost(postFromDb);
    }


    @Test
    void deletePost_userDoesNotExist_throwsUserNotFoundException() {
        Post post = new Post();
        post.setId(200L);
        post.setTitle("Test Post");
        post.setContent("Test Content");

        when(userPersistenceOutputPort.existsById(user.getId())).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> postService.deletePost(user, post.getId()));

        verify(postPersistenceOutputPort, never()).deletePost(any());
    }

    @Test
    void deletePost_postNotFound_throwsPostNotFoundException() throws PostNotFoundException {
        Post post = new Post();
        post.setId(300L);
        post.setTitle("Missing Post");
        post.setContent("Missing Content");

        when(userPersistenceOutputPort.existsById(user.getId())).thenReturn(true);
        when(postPersistenceOutputPort.getPostById(post.getId())).thenThrow(new PostNotFoundException("Post not found"));

        assertThrows(PostNotFoundException.class, () -> postService.deletePost(user, post.getId()));
    }


    @Test
    void deletePost_userIsNotOwner_throwsAccessDeniedException() throws UserNotFoundException, PostNotFoundException {
        Post post = new Post();
        post.setId(400L);
        post.setTitle("Someone else's post");

        User anotherUser = new User();
        anotherUser.setId(999L);

        post.setUser(anotherUser);

        when(userPersistenceOutputPort.existsById(user.getId())).thenReturn(true);
        when(postPersistenceOutputPort.getPostById(post.getId())).thenReturn(post);

        assertThrows(AccessDeniedException.class, () -> postService.deletePost(user, post.getId()));
    }




    @Test
    public void testEditPost_Success() throws Exception, PostNotFoundException, UserNotFoundException {
        Post updatedPost = new Post();
        updatedPost.setId(existingPost.getId());
        updatedPost.setTitle("New Title");
        updatedPost.setContent("New Content");

        when(userPersistenceOutputPort.existsById(user.getId())).thenReturn(true);
        when(postPersistenceOutputPort.getPostById(updatedPost.getId())).thenReturn(existingPost);
        when(postPersistenceOutputPort.savePost(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Post result = postService.editPost(user, updatedPost);

        assertNotNull(result);
        assertEquals("New Title", result.getTitle());
        assertEquals("New Content", result.getContent());
        assertEquals(user, result.getUser());
        assertNotNull(result.getUpdatedDate());

        verify(userPersistenceOutputPort).existsById(user.getId());
        verify(postPersistenceOutputPort).getPostById(updatedPost.getId());
        verify(postPersistenceOutputPort).savePost(any(Post.class));
    }


    @Test
    public void testEditPost_UserNotFound_ThrowsException() {
        Post updatedPost = new Post();
        updatedPost.setId(10L);
        updatedPost.setTitle("New Title");
        updatedPost.setContent("New Content");

        when(userPersistenceOutputPort.existsById(user.getId())).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> postService.editPost(user, updatedPost));
        verify(userPersistenceOutputPort).existsById(user.getId());
        verifyNoMoreInteractions(postPersistenceOutputPort);
    }

    @Test
    public void testEditPost_PostNotOwnedByUser_ThrowsAccessDenied() throws PostNotFoundException {
        Post updatedPost = new Post();
        updatedPost.setId(existingPost.getId());
        updatedPost.setTitle("New Title");
        updatedPost.setContent("New Content");

        User anotherUser = new User();
        anotherUser.setId(99L);

        when(userPersistenceOutputPort.existsById(anotherUser.getId())).thenReturn(true);
        when(postPersistenceOutputPort.getPostById(updatedPost.getId())).thenReturn(existingPost);

        assertThrows(AccessDeniedException.class, () -> postService.editPost(anotherUser, updatedPost));
    }

    @ParameterizedTest
    @MethodSource("invalidInputs")
    public void testEditPost_InvalidInput_ThrowsIllegalArgumentException(String input) {
        Post updatedPost = new Post();
        updatedPost.setId(10L);
        updatedPost.setTitle(input);
        updatedPost.setContent("Content");
        assertThrows(IllegalArgumentException.class, () -> postService.editPost(user, updatedPost));
    }


    @ParameterizedTest
    @MethodSource("invalidInputs")
    public void testEditPost_InvalidContent_ThrowsIllegalArgumentException(String input) {
        Post updatedPost = new Post();
        updatedPost.setId(10L);
        updatedPost.setTitle("title");
        updatedPost.setContent(input);
        assertThrows(IllegalArgumentException.class, () -> postService.editPost(user, updatedPost));
    }
}



