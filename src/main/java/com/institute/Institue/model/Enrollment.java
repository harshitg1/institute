package com.institute.Institue.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Enrollment {
    private String id;
    private String userId;
    private String courseId;

    public Enrollment() {}

    public Enrollment(String id, String userId, String courseId) { this.id = id; this.userId = userId; this.courseId = courseId; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
}

