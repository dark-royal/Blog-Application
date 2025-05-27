package org.example.infrastructure.adapters.input.rest.mapper;

import org.example.domain.models.User;
import org.example.infrastructure.adapters.input.rest.data.request.LoginUserRequest;
import org.example.infrastructure.adapters.input.rest.data.request.RegisterUserRequest;
import org.example.infrastructure.adapters.input.rest.data.request.ResetPasswordRequest;
import org.example.infrastructure.adapters.input.rest.data.response.LoginUserResponse;
import org.example.infrastructure.adapters.input.rest.data.response.RegisterUserResponse;
import org.example.infrastructure.adapters.input.rest.data.response.ResetPasswordResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserRestMapper {


    @Mapping(target = "password", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(source = "role", target = "role")
    User toUser(RegisterUserRequest registerUserRequest);


    RegisterUserResponse toCreateUserResponse(User user);

    @Mapping(source = "accessToken", target = "accessToken")
    @Mapping(source = "expiresIn", target = "expiresIn")
    @Mapping(source = "refreshExpiresIn", target = "refreshExpiresIn")
    @Mapping(source = "refreshToken", target = "refreshToken")
    @Mapping(source = "tokenType", target = "tokenType")
    @Mapping(source = "idToken", target = "idToken")
    @Mapping(source = "scope", target = "scope")
    LoginUserResponse toLoginUserResponse(User authenticatedUser);



    User toUser(LoginUserRequest loginUserRequest);

    User toUser(ResetPasswordRequest resetPasswordRequest);

    ResetPasswordResponse toResetPasswordResponse(User user);
}
