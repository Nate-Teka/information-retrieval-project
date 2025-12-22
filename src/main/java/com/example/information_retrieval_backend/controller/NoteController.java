package com.example.information_retrieval_backend.controller;

import com.example.information_retrieval_backend.dto.NoteDto;
import com.example.information_retrieval_backend.dto.NoteResponse;
import com.example.information_retrieval_backend.model.Note;
import com.example.information_retrieval_backend.service.NoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    // Create a new note
    @PostMapping
    public ResponseEntity<Note> createNote(@RequestBody NoteDto dto) {
        Note created = noteService.create(dto);
        return ResponseEntity.ok(created);
    }

    // Update an existing note
    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable Long id, @RequestBody NoteDto dto) {
        try {
            Note updated = noteService.update(id, dto);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete a note
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        try {
            noteService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    // Get all notes
    @GetMapping
    public List<NoteResponse> getAllNotes() {
        return noteService.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    // Search notes by query text and/or tag
    @GetMapping("/search")
    public List<NoteResponse> searchNotes(@RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "tag", required = false) String tag) {
        return noteService.search(q, tag);
    }

    public NoteResponse toDto(Note note) {
        NoteResponse dto = new NoteResponse();
        dto.setId(note.getId());
        dto.setTitle(note.getTitle());
        dto.setContent(note.getContent());
        dto.setLastUpdatedAt(note.getLastModifiedDate());
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
