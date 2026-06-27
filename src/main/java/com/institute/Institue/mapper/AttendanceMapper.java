package com.institute.Institue.mapper;

import com.institute.Institue.dto.AttendanceResponse;
import com.institute.Institue.model.Attendance;
import com.institute.Institue.model.Batch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Mapper(config = SharedMapper.class)
public interface AttendanceMapper {

    @Mapping(target = "id", expression = "java(attendance.getId() != null ? attendance.getId().toString() : null)")
    @Mapping(target = "studentId", expression = "java(attendance.getStudent() != null && attendance.getStudent().getId() != null ? attendance.getStudent().getId().toString() : null)")
    @Mapping(target = "studentName", expression = "java(resolveStudentName(attendance))")
    @Mapping(target = "status", expression = "java(attendance.getStatus() != null ? attendance.getStatus().name() : null)")
    AttendanceResponse.AttendanceRecordResponse toRecordResponse(Attendance attendance);

    default AttendanceResponse toAttendanceResponse(Batch batch, LocalDate date, List<Attendance> records) {
        int present = 0, absent = 0, late = 0, excused = 0;
        List<AttendanceResponse.AttendanceRecordResponse> responseRecords = new ArrayList<>();

        for (Attendance record : records) {
            switch (record.getStatus()) {
                case PRESENT -> present++;
                case ABSENT -> absent++;
                case LATE -> late++;
                case EXCUSED -> excused++;
            }
            responseRecords.add(toRecordResponse(record));
        }

        return AttendanceResponse.builder()
                .batchId(batch.getId() != null ? batch.getId().toString() : null)
                .batchName(batch.getName())
                .date(date)
                .totalStudents(records.size())
                .present(present)
                .absent(absent)
                .late(late)
                .excused(excused)
                .records(responseRecords)
                .build();
    }

    default String resolveStudentName(Attendance attendance) {
        if (attendance.getStudent() == null) {
            return null;
        }
        String fn = attendance.getStudent().getFirstName();
        String ln = attendance.getStudent().getLastName();
        String name = ((fn != null ? fn : "") + " " + (ln != null ? ln : "")).trim();
        return name.isEmpty() ? attendance.getStudent().getEmail() : name;
    }
}
