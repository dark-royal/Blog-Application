package org.example.infrastructure.adapters.output.persistence.mapper;

import org.example.domain.models.Post;
import org.example.infrastructure.adapters.output.persistence.entity.PostEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserPersistenceMapper.class})
public interface PostPersistenceMapper {


    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "publishedDate", source = "publishedDate")
    @Mapping(target = "updatedDate", source = "updatedDate")
    @Mapping(target = "user", source = "user")
    PostEntity toEntity(Post post);


    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "publishedDate", source = "publishedDate")
    @Mapping(target = "updatedDate", source = "updatedDate")
    @Mapping(target = "user", source = "user")
    Post toPost(PostEntity entity);
}
