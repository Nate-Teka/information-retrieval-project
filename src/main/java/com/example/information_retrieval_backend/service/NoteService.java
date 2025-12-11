package com.example.information_retrieval_backend.service;

import com.example.information_retrieval_backend.model.Note;
import com.example.information_retrieval_backend.repository.NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class NoteService {

    private final NoteRepository repo;

    public NoteService(NoteRepository repo) {
        this.repo = repo;
    }

    public List<Note> findAll() {
        return repo.findAll().stream()
                .sorted(Comparator.comparing(Note::getLastModifiedDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public Optional<Note> findById(Long id) {
        return repo.findById(id);
    }

    public Note create(Note note) {
        note.setId(null);
        return repo.save(note);
    }

    public Note update(Long id, Note updated) {
        return repo.findById(id).map(existing -> {
            existing.setTitle(updated.getTitle());
            existing.setContent(updated.getContent());
            existing.setTags(updated.getTags());
            // lastModifiedDate will update via auditing
            return repo.save(existing);
        }).orElseThrow(() -> new NoSuchElementException("Note not found: " + id));
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    /**
     * Combine full-text search results and optional tag filter.
     * If tag is provided, filter results to those that contain the tag.
     * Results from FTS are already ordered by relevance.
     */
    public List<Note> search(String query, String tag) {
        if (query == null || query.isBlank()) {
            // If no query, return simple tag-filtered list or all
            if (tag == null || tag.isBlank()) return findAll();
            return repo.findByTag(tag);
        }
        List<Note> ftsResults = repo.searchByFullText(query);
        if (tag == null || tag.isBlank()) {
            return ftsResults;
        }
        String lowTag = tag.toLowerCase();
        return ftsResults.stream()
                .filter(n -> n.getTags() != null && Arrays.stream(n.getTags().split(","))
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .anyMatch(t -> t.equals(lowTag)))
                .collect(Collectors.toList());
    }
}

