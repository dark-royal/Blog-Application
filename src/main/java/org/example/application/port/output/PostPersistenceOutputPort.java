package org.example.application.port.output;

import org.example.domain.exceptions.PostNotFoundException;
import org.example.domain.models.Post;

import java.util.List;

public interface PostPersistenceOutputPort {

    Post savePost(Post post);


    void deletePost(Post post);

    Post getPostById(Long id) throws PostNotFoundException;

    boolean existsById(Long id);

    boolean existsByTitleAndUserId(String title, Long id);

    List<Post> getAllPostByUserId(Long id);

}
