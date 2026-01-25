package com.institute.Institue.mapper;

import com.institute.Institue.dto.UserResponse;
import com.institute.Institue.model.Role;
import com.institute.Institue.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", config = SharedMapper.class)
public interface UserMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "organization", source = "organization")
    @Mapping(target = "role", source = "role",qualifiedByName = "roleToString")
    UserResponse toDto(User user);
    @Named ("roleToString")
    default String roleToString(Role role) {
        return role != null ? role.getName() : null;
    }
}
