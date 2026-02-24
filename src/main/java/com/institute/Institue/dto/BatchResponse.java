package com.institute.Institue.dto;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchResponse {
    private String id;
    private String name;
    private String instructorId;
    private String instructorName;
    private String duration;
    private String startTime;
    private String endTime;
    private long studentCount;
    private boolean active;
    private Instant createdAt;
    private List<StudentResponse> students;
}
