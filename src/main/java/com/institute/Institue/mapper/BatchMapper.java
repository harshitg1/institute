package com.institute.Institue.mapper;

import com.institute.Institue.dto.BatchResponse;
import com.institute.Institue.dto.StudentResponse;
import com.institute.Institue.model.Batch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = SharedMapper.class, uses = {StudentMapper.class})
public interface BatchMapper extends BaseMapper<Batch, BatchResponse> {

    @Override
    @Mapping(target = "id", expression = "java(batch.getId() != null ? batch.getId().toString() : null)")
    @Mapping(target = "instructorId", expression = "java(batch.getInstructor() != null && batch.getInstructor().getId() != null ? batch.getInstructor().getId().toString() : null)")
    @Mapping(target = "instructorName", expression = "java(resolveInstructorName(batch))")
    @Mapping(target = "studentCount", ignore = true)
    @Mapping(target = "active", source = "active")
    @Mapping(target = "students", ignore = true)
    @Mapping(target = "createdAt", source = "createdAt")
    BatchResponse toDto(Batch batch);

    default BatchResponse toDto(Batch batch, long studentCount, List<StudentResponse> students) {
        BatchResponse response = toDto(batch);
        response.setStudentCount(studentCount);
        response.setStudents(students);
        return response;
    }

    @Override
    @Mapping(target = "id", expression = "java(dto.getId() != null ? java.util.UUID.fromString(dto.getId()) : null)")
    @Mapping(target = "instructor", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "batchStudents", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Batch toEntity(BatchResponse dto);

    default String resolveInstructorName(Batch batch) {
        if (batch.getInstructor() == null) {
            return null;
        }
        String fn = batch.getInstructor().getFirstName();
        String ln = batch.getInstructor().getLastName();
        String name = ((fn != null ? fn : "") + " " + (ln != null ? ln : "")).trim();
        return name.isEmpty() ? batch.getInstructor().getEmail() : name;
    }
}
