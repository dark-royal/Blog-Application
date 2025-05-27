package org.example.user;

import lombok.RequiredArgsConstructor;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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


    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(100L);
        user.setUsername("admin");
        user.setEmail("admin@example.com");
    }


    @Test
    public void testThatPostCanBeCreated() throws PostAlreadyExistsException, UserNotFoundException {

        Post post = new Post();
        post.setId(200L);
        post.setTitle("My Blog Application");
        post.setContent("I love to create content");

        when(userPersistenceOutputPort.existsById(user.getId())).thenReturn(true);

        when(postPersistenceOutputPort.existsByTitle(post.getTitle())).thenReturn(false);

        Post savedPost = new Post();
        savedPost.setId(post.getId());
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
        verify(postPersistenceOutputPort).existsByTitle(post.getTitle());
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
    public void testCreatePost_ShouldThrow_WhenTitleExists() throws UserNotFoundException {

        Post post = new Post();
        post.setTitle("Existing Title");
        post.setContent("Existing Content");

        when(userPersistenceOutputPort.existsById(user.getId())).thenReturn(true);
        when(postPersistenceOutputPort.existsByTitle(post.getTitle())).thenReturn(true);

        assertThrows(PostAlreadyExistsException.class, () -> {
            postService.createPost(user, post);
        });

        verify(userPersistenceOutputPort).existsById(user.getId());
        verify(postPersistenceOutputPort).existsByTitle(post.getTitle());
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
}
