package com.institute.Institue.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Course {
    private String id;
    private String title;

    public Course() {}

    public Course(String id, String title) { this.id = id; this.title = title; }

    public void setId(String id) { this.id = id; }

    public void setTitle(String title) { this.title = title; }
}

