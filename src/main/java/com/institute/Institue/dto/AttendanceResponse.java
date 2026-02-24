package com.institute.Institue.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceResponse {
    private String batchId;
    private String batchName;
    private LocalDate date;
    private int totalStudents;
    private int present;
    private int absent;
    private int late;
    private int excused;
    private List<AttendanceRecordResponse> records;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AttendanceRecordResponse {
        private String id;
        private String studentId;
        private String studentName;
        private String status;
        private String remarks;
    }
}
