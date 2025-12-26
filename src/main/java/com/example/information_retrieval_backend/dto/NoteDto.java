package com.example.information_retrieval_backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class NoteDto {
    private String title;
    private String content;
    private List<String> tags;      // auto-generated tags (optional)
    private List<String> userTags;  // user-added tags
    private List<String> allTags;   // sent from frontend, can be userTags
    private LocalDateTime lastModifiedDate;

    // --- Getters ---
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public List<String> getTags() { return tags; }
    public List<String> getUserTags() { return userTags; }
    public List<String> getAllTags() { return allTags; }
    public LocalDateTime getLastModifiedDate() { return lastModifiedDate; }

    // --- Setters ---
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public void setUserTags(List<String> userTags) { this.userTags = userTags; }
    public void setAllTags(List<String> allTags) { this.allTags = allTags; }
    public void setLastModifiedDate(LocalDateTime lastModifiedDate) { this.lastModifiedDate = lastModifiedDate; }
}
