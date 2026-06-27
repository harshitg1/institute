package com.institute.Institue.mapper;

import com.institute.Institue.dto.CourseDto;
import com.institute.Institue.dto.StudentResponse;
import com.institute.Institue.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = SharedMapper.class, uses = CourseMapper.class)
public interface StudentMapper extends BaseMapper<User, StudentResponse> {

    @Override
    @Mapping(target = "id", expression = "java(student.getId() != null ? student.getId().toString() : null)")
    @Mapping(target = "batchId", ignore = true)
    @Mapping(target = "batchName", ignore = true)
    @Mapping(target = "courses", ignore = true)
    @Mapping(target = "status", expression = "java(student.getStudentStatus() != null ? student.getStudentStatus().name() : \"ACTIVE\")")
    @Mapping(target = "createdAt", source = "createdAt")
    StudentResponse toDto(User student);

    default StudentResponse toDto(User student, String batchId, String batchName, List<CourseDto> courses) {
        StudentResponse response = toDto(student);
        response.setBatchId(batchId);
        response.setBatchName(batchName);
        response.setCourses(courses);
        return response;
    }

    @Override
    @Mapping(target = "id", expression = "java(dto.getId() != null ? java.util.UUID.fromString(dto.getId()) : null)")
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
    User toEntity(StudentResponse dto);
}
