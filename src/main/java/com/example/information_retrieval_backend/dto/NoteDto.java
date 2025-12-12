package com.example.information_retrieval_backend.dto;



import java.time.LocalDateTime;
import java.util.List;

public class NoteDto {

    private String title;
    private String content;
    private List<String> tags;
    private List<String> userTags;
    private List<String> allTags;
    private LocalDateTime lastModifiedDate;
    // --- Getters ---

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public List<String> getUserTags() {
        return userTags;
    }
    public List<String> getTags() {
        return userTags;
    }
    public List<String> getallUserTags() {
        return userTags;
    }

    // --- Setters ---

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setUserTags(List<String> userTags) {
        this.userTags = userTags;
    }
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    public void setAllTags(List<String> allTags) {
        this.allTags = allTags;
    }
}

