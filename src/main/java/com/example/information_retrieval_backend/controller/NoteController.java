package com.example.information_retrieval_backend.controller;

import com.example.information_retrieval_backend.dto.NoteDto;
import com.example.information_retrieval_backend.dto.NoteResponse;
import com.example.information_retrieval_backend.model.Note;
import com.example.information_retrieval_backend.service.NoteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public List<NoteResponse> getAll() {
        return noteService.findAll().stream()
                .map(noteService::toDtoPublic)
                .toList();
    }

    @GetMapping("/search")
    public List<NoteResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String tag
    ) {
        return noteService.search(q, tag);
    }

    @PostMapping
    public NoteResponse create(@RequestBody NoteDto dto) {
        Note note = noteService.create(dto);
        return noteService.toDtoPublic(note);
    }

    @PutMapping("/{id}")
    public NoteResponse update(@PathVariable Long id, @RequestBody NoteDto dto) {
        Note note = noteService.update(id, dto);
        return noteService.toDtoPublic(note);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        noteService.delete(id);
    }
}
