package com.institute.Institue.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRequest {

    @NotBlank(message = "Batch ID is required")
    private String batchId;

    @NotBlank(message = "Date is required (YYYY-MM-DD)")
    private String date;

    @NotEmpty(message = "At least one attendance record is required")
    @Valid
    private List<AttendanceRecord> records;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AttendanceRecord {
        @NotBlank(message = "Student ID is required")
        private String studentId;

        @NotBlank(message = "Status is required (PRESENT, ABSENT, LATE, EXCUSED)")
        private String status;

        private String remarks;
    }
}
