package com.institute.Institue.mapper;

import com.institute.Institue.dto.UserResponse;
import com.institute.Institue.model.Role;
import com.institute.Institue.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", config = SharedMapper.class)
public interface UserMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "organizationId", expression = "java(user.getOrganizationId() == null ? null : user.getOrganizationId().toString())")
    @Mapping(target = "roles", source = "roles", qualifiedByName = "mapRoles")
    UserResponse toDto(User user);

    @Named("mapRoles")
    default List<String> mapRoles(Set<Role> roles) {
        if (roles == null) return null;
        return roles.stream().map(Role::getName).collect(Collectors.toList());
    }
}
