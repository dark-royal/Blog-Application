package org.example.application.port.input;

import org.example.domain.exceptions.PostNotFoundException;
import org.example.domain.models.Post;

public interface ViewPostUseCase {

    Post viewPost(Long id) throws PostNotFoundException;


}
