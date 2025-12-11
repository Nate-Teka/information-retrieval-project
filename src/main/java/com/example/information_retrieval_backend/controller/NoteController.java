package com.example.information_retrieval_backend.controller;

import com.example.information_retrieval_backend.model.Note;
import com.example.information_retrieval_backend.service.NoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService svc;

    public NoteController(NoteService svc) {
        this.svc = svc;
    }

    @GetMapping
    public List<Note> getAll() {
        return svc.findAll();
    }

    @PostMapping
    public ResponseEntity<Note> create(@RequestBody Note note) {
        Note created = svc.create(note);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Note> update(@PathVariable Long id, @RequestBody Note note) {
        try {
            Note updated = svc.update(id, note);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        svc.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search endpoint: GET /api/notes/search?q=...&tag=...
     */
    @GetMapping("/search")
    public List<Note> search(@RequestParam(name = "q", required = false) String q,
                             @RequestParam(name = "tag", required = false) String tag) {
        return svc.search(q, tag);
    }
}

