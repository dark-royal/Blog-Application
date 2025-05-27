package org.example.infrastructure.adapters.output.persistence.mapper;


import org.example.domain.models.User;
import org.example.infrastructure.adapters.output.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface UserPersistenceMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "emailVerified", source = "emailVerified")
    @Mapping(target = "enabled", source = "enabled")
    @Mapping(target = "password", source = "password")
    UserEntity toEntity(User user);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "password", source = "password")
    @Mapping(target = "emailVerified", source = "emailVerified")
    @Mapping(target = "enabled", source = "enabled")
    User toModel(UserEntity entity);
}

