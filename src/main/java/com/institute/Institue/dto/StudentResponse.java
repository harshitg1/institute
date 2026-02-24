package com.institute.Institue.dto;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentResponse {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String status;
    private String batchId;
    private String batchName;
    private List<CourseDto> courses;
    private Instant createdAt;
}
