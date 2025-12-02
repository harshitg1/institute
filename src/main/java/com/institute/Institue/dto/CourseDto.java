package com.institute.Institue.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CourseDto {
    private String id;
    private String title;

    public CourseDto() {}

    public CourseDto(String id, String title) {
        this.id = id;
        this.title = title;
    }

}

