package com.example.information_retrieval_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "note_tokens", joinColumns = @JoinColumn(name = "note_id"))
    @Column(name = "token")
    private Set<String> tokens = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "note_title_tokens", joinColumns = @JoinColumn(name = "note_id"))
    @Column(name = "token")
    private Set<String> titleTokens = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "note_content_tokens", joinColumns = @JoinColumn(name = "note_id"))
    @Column(name = "token")
    private Set<String> contentTokens = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "note_user_tags", joinColumns = @JoinColumn(name = "note_id"))
    @Column(name = "tag")
    private Set<String> userTags = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "note_tags", joinColumns = @JoinColumn(name = "note_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    // --- GETTERS AND SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getLastModifiedDate() { return lastModifiedDate; }
    public void setLastModifiedDate(LocalDateTime lastModifiedDate) { this.lastModifiedDate = lastModifiedDate; }

    public Set<String> getTokens() { return tokens; }
    public void setTokens(Set<String> tokens) { this.tokens = tokens; }

    public Set<String> getTitleTokens() { return titleTokens; }
    public void setTitleTokens(Set<String> titleTokens) { this.titleTokens = titleTokens; }

    public Set<String> getContentTokens() { return contentTokens; }
    public void setContentTokens(Set<String> contentTokens) { this.contentTokens = contentTokens; }

    public Set<String> getUserTags() { return userTags; }
    public void setUserTags(Set<String> userTags) { this.userTags = userTags; }

    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) { this.tags = tags; }
}
