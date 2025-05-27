package org.example.application.port.output;

import org.example.domain.exceptions.PostNotFoundException;
import org.example.domain.models.Post;

public interface PostPersistenceOutputPort {

    Post savePost(Post post);

    Post getByTitle(String title) throws PostNotFoundException;

    boolean existsByTitle(String title);



}
