package org.example.infrastructure.adapters.input.rest.mapper;

import org.example.domain.models.Post;
import org.example.infrastructure.adapters.input.rest.data.request.CreatePostRequest;
import org.example.infrastructure.adapters.input.rest.data.response.CreatePostResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PostRestMapper {


    Post toPost(CreatePostRequest createPostRequest);

    CreatePostResponse toCreatePostResponse(Post createdPost);

}
