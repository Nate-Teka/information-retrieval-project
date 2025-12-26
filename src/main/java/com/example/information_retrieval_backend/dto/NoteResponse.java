package com.example.information_retrieval_backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class NoteResponse {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime lastUpdatedAt;
    private List<String> tags;     // auto-generated, optional
    private List<String> userTags; // only user tags sent to frontend
    private List<String> allTags;  // optional for frontend

    // --- Getters ---
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }
    public List<String> getTags() { return tags; }
    public List<String> getUserTags() { return userTags; }
    public List<String> getAllTags() { return allTags; }

    // --- Setters ---
    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public void setUserTags(List<String> userTags) { this.userTags = userTags; }
    public void setAllTags(List<String> allTags) { this.allTags = allTags; }
}
