package com.institute.Institue.mapper;

import com.institute.Institue.dto.UserResponse;
import com.institute.Institue.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = SharedMapper.class)
public interface UserMapper extends BaseMapper<User, UserResponse> {

    @Override
    @Mapping(target = "id", source = "id")
    @Mapping(target = "role", expression = "java(user.getRole() != null ? user.getRole().getRole().name() : null)")
    @Mapping(target = "organizationId", expression = "java(user.getOrganization() != null ? user.getOrganization().getId() : null)")
    @Mapping(target = "organizationName", expression = "java(user.getOrganization() != null ? user.getOrganization().getName() : null)")
    UserResponse toDto(User user);

    @Override
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "studentStatus", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "accountNonExpired", ignore = true)
    @Mapping(target = "accountNonLocked", ignore = true)
    @Mapping(target = "credentialsNonExpired", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    User toEntity(UserResponse dto);
}
