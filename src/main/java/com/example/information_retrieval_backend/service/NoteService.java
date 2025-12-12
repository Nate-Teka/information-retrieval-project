package com.example.information_retrieval_backend.service;

import com.example.information_retrieval_backend.dto.NoteDto;
import com.example.information_retrieval_backend.dto.NoteResponse;
import com.example.information_retrieval_backend.model.Note;
import com.example.information_retrieval_backend.repository.NoteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class NoteService {

    private final NoteRepository repo;
    private final TextProcessingService textProcessing;
    private final TfidfService tfidfService;

    public NoteService(NoteRepository repo,
            TextProcessingService textProcessing,
            TfidfService tfidfService) {
        this.repo = repo;
        this.textProcessing = textProcessing;
        this.tfidfService = tfidfService;
    }

    // Create a new note with token preprocessing and tag generation
    public Note create(NoteDto dto) {
        Note note = new Note();
        note.setTitle(dto.getTitle());
        note.setContent(dto.getContent());
        note.setLastModifiedDate(LocalDateTime.now());

        // preprocess tokens
        note.setTokens(new HashSet<>(textProcessing.preprocess(dto.getContent())));

        // generate tags
        List<String> autoTags = tfidfService.generateTagsForNoteContent(dto.getContent(), 6);
        List<String> merged = tfidfService.mergeUserTags(autoTags, dto.getUserTags());
        note.setTags(new HashSet<>(merged));
        note.setUserTags(new HashSet<>(dto.getUserTags()));


        return repo.save(note);
    }

    // Update an existing note
    public Note update(Long id, NoteDto dto) {
        Note note = repo.findById(id).orElseThrow(NoSuchElementException::new);

        note.setTitle(dto.getTitle());
        note.setContent(dto.getContent());
        note.setLastModifiedDate(LocalDateTime.now());

        // preprocess tokens
        note.setTokens(new HashSet<>(textProcessing.preprocess(dto.getContent())));

        // generate tags
        List<String> autoTags = tfidfService.generateTagsForNoteContent(dto.getContent(), 6);
        List<String> merged = tfidfService.mergeUserTags(autoTags, dto.getUserTags());
        note.setTags(new HashSet<>(merged));

        return repo.save(note);
    }

    public void delete(Long id) {
        if (!repo.existsById(id))
            throw new NoSuchElementException();
        repo.deleteById(id);
    }

    public List<Note> findAll() {
        return repo.findAll();
    }

    // Search notes by query text and/or tag
    public List<NoteResponse> search(String q, String tag) {
        List<Note> notes = repo.findAll();

        if (q != null && !q.isEmpty()) {
            Set<String> queryTokens = new HashSet<>(textProcessing.preprocess(q));
            notes = notes.stream()
                    .filter(n -> n.getTokens() != null && !Collections.disjoint(n.getTokens(), queryTokens))
                    .collect(Collectors.toList());
        }

        if (tag != null && !tag.isEmpty()) {
            notes = notes.stream()
                    .filter(n -> (n.getTags() != null && n.getTags().stream().anyMatch(t -> t.contains(tag)))
                            || (n.getUserTags() != null && n.getUserTags().stream().anyMatch(t -> t.contains(tag))))
                    .collect(Collectors.toList());
        }

        return notes.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    public NoteResponse toDto(Note note) {
        NoteResponse dto = new NoteResponse();
        dto.setId(note.getId());
        dto.setTitle(note.getTitle());
        dto.setContent(note.getContent());

        // Convert sets to lists, handle nulls
        dto.setTokens(note.getTokens() != null ? new ArrayList<>(note.getTokens()) : new ArrayList<>());
        dto.setAllTags(
                Stream.concat(
                        note.getTags() != null ? note.getTags().stream() : Stream.empty(),
                        note.getUserTags() != null ? note.getUserTags().stream() : Stream.empty())
                        .collect(Collectors.toList()));

        return dto;
    }
}
