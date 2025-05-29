package org.example.infrastructure.adapters.input.rest.mapper;

import org.example.domain.models.Post;
import org.example.infrastructure.adapters.input.rest.data.request.CreatePostRequest;
import org.example.infrastructure.adapters.input.rest.data.request.EditPostRequest;
import org.example.infrastructure.adapters.input.rest.data.response.CreatePostResponse;
import org.example.infrastructure.adapters.input.rest.data.response.EditPostResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PostRestMapper {



    Post toPost(CreatePostRequest createPostRequest);


    CreatePostResponse toCreatePostResponse(Post createdPost);


    Post toPost(EditPostRequest editPostRequest);

    @Mapping(target = "editedAt", source = "editedAt")
    EditPostResponse toEditPostResponse(Post post);

}
