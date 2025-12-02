package com.institute.Institue.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoProgress {
    private String id;
    private String userId;
    private String lessonId;
    private int secondsWatched;

    public VideoProgress() {}

    public VideoProgress(String id, String userId, String lessonId, int secondsWatched) {
        this.id = id; this.userId = userId; this.lessonId = lessonId; this.secondsWatched = secondsWatched;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getLessonId() { return lessonId; }
    public void setLessonId(String lessonId) { this.lessonId = lessonId; }
    public int getSecondsWatched() { return secondsWatched; }
    public void setSecondsWatched(int secondsWatched) { this.secondsWatched = secondsWatched; }
}

