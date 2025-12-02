package com.institute.Institue.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Lesson {
    private String id;
    private String title;

    public Lesson() {}

    public Lesson(String id, String title) { this.id = id; this.title = title; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}

