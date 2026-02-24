package com.institute.Institue.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDto {
    private String id;
    private String title;
    private String description;
    private BigDecimal price;
    private String thumbnailUrl;
    private Integer durationHours;
    private boolean published;
    private long enrollmentCount;
    private Instant createdAt;

    // Legacy constructor for backward compatibility
    public CourseDto(String id, String title) {
        this.id = id;
        this.title = title;
    }
}
