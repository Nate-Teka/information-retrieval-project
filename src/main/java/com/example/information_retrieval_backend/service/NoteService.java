package com.example.information_retrieval_backend.service;

import com.example.information_retrieval_backend.dto.NoteDto;
import com.example.information_retrieval_backend.dto.NoteResponse;
import com.example.information_retrieval_backend.model.Note;
import com.example.information_retrieval_backend.repository.NoteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

        note.setTokens(new HashSet<>(textProcessing.preprocess(dto.getContent())));

        List<String> autoTags = tfidfService.generateTagsForNoteContent(dto.getContent(), 6);
        List<String> userTags = dto.getUserTags() != null ? dto.getUserTags() : new ArrayList<>();

        List<String> merged = tfidfService.mergeUserTags(autoTags, userTags);

        note.setTags(new HashSet<>(merged));
        note.setUserTags(new HashSet<>(userTags));

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

        // Preprocess query
        List<String> queryTokens = q != null && !q.isEmpty() ? textProcessing.preprocess(q) : Collections.emptyList();

        // Prepare IDF map
        Map<String, Integer> df = tfidfService.computeDfAll();
        int N = (int) repo.count();
        Map<String, Double> idfMap = tfidfService.computeIdf(df, Math.max(N, 1));

        // Filter by tag if provided
        if (tag != null && !tag.isEmpty()) {
            String lowerTag = tag.toLowerCase();
            notes = notes.stream()
                    .filter(n -> (n.getTags() != null && n.getTags().stream().anyMatch(t -> t.contains(lowerTag)))
                            || (n.getUserTags() != null
                                    && n.getUserTags().stream().anyMatch(t -> t.contains(lowerTag))))
                    .collect(Collectors.toList());
        }

        // Compute TF-IDF score for each note
        List<NoteScore> scoredNotes = notes.stream()
                .map(n -> new NoteScore(n,
                        tfidfService.computeRelevanceScore(new ArrayList<>(n.getTokens()), queryTokens, idfMap)))
                .filter(ns -> ns.score > 0) // optional: only notes that match
                .sorted((a, b) -> Double.compare(b.score, a.score)) // descending relevance
                .collect(Collectors.toList());

        // Return NoteResponse DTOs
        return scoredNotes.stream()
                .map(ns -> toDto(ns.note)) // you can merge allTags here if you want
                .collect(Collectors.toList());
    }

    // Helper class for scoring
    private static class NoteScore {
        Note note;
        double score;

        NoteScore(Note note, double score) {
            this.note = note;
            this.score = score;
        }
    }

    public NoteResponse toDto(Note note) {
        NoteResponse dto = new NoteResponse();
        dto.setId(note.getId());
        dto.setTitle(note.getTitle());
        dto.setContent(note.getContent());
        dto.setTokens(note.getTokens() != null ? new ArrayList<>(note.getTokens()) : new ArrayList<>());

        List<String> mergedTags = new ArrayList<>();
        if (note.getUserTags() != null)
            mergedTags.addAll(note.getUserTags());
        if (note.getTags() != null)
            mergedTags.addAll(note.getTags());

        dto.setTags(note.getTags() != null ? new ArrayList<>(note.getTags()) : new ArrayList<>());
        dto.setUserTags(note.getUserTags() != null ? new ArrayList<>(note.getUserTags()) : new ArrayList<>());
        dto.setAllTags(mergedTags);
        return dto;
    }

}
