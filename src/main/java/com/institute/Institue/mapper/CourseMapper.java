package com.institute.Institue.mapper;

import com.institute.Institue.dto.CourseDto;
import com.institute.Institue.model.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = SharedMapper.class)
public interface CourseMapper extends BaseMapper<Course, CourseDto> {

    @Override
    @Mapping(target = "id", expression = "java(course.getId() != null ? course.getId().toString() : null)")
    @Mapping(target = "enrollmentCount", ignore = true)
    @Mapping(target = "createdAt", source = "createdAt")
    CourseDto toDto(Course course);

    default CourseDto toDto(Course course, long enrollmentCount) {
        CourseDto dto = toDto(course);
        dto.setEnrollmentCount(enrollmentCount);
        return dto;
    }

    @Override
    @Mapping(target = "id", expression = "java(dto.getId() != null ? java.util.UUID.fromString(dto.getId()) : null)")
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Course toEntity(CourseDto dto);
}
