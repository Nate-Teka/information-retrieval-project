package com.example.information_retrieval_backend.dto;

import java.util.List;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class NoteResponse {

    private Long id;
    private String title;
    private String content;
    private List<String> tokens = new ArrayList<>();
    private List<String> tags = new ArrayList<>();
    private List<String> userTags = new ArrayList<>();
    private List<String> allTags = new ArrayList<>();
    private LocalDateTime lastUpdatedAt;

    // --- Getters ---
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<String> getUserTags() {
        return userTags;
    }

    public List<String> getAllTags() {
        return allTags;
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    // --- Setters ---
    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setUserTags(List<String> userTags) {
        this.userTags = userTags;
    }

    public void setAllTags(List<String> allTags) {
        this.allTags = allTags;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

}
