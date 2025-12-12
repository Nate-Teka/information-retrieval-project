package com.example.information_retrieval_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "notes")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "note_tokens", joinColumns = @JoinColumn(name = "note_id"))
    @Column(name = "token")
    private Set<String> tokens = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "note_tags", joinColumns = @JoinColumn(name = "note_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "note_user_tags", joinColumns = @JoinColumn(name = "note_id"))
    @Column(name = "user_tag")
    private Set<String> userTags = new HashSet<>();

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

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public Set<String> getTokens() {
        return tokens;
    }

    public Set<String> getTags() {
        return tags;
    }

    public Set<String> getUserTags() {
        return userTags;
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

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public void setTokens(Set<String> tokens) {
        this.tokens = tokens;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public void setUserTags(Set<String> userTags) {
        this.userTags = userTags;
    }
}
