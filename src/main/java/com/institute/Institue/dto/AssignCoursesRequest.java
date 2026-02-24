package com.institute.Institue.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignCoursesRequest {

    @NotEmpty(message = "At least one course ID is required")
    private List<String> courseIds;
}
