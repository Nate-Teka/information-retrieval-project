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

    public Note create(NoteDto dto) {
        Note note = new Note();
        note.setTitle(dto.getTitle());
        note.setContent(dto.getContent());
        note.setLastModifiedDate(LocalDateTime.now());

        // preprocess tokens
        // preprocess tokens
        note.setTokens(new HashSet<>(textProcessing.preprocess(dto.getContent())));
        tfidfService.printTfidf(note);

        // 1️⃣ Save user tags first (persist to note_user_tags table)
        Set<String> userTags = dto.getUserTags() != null
                ? dto.getUserTags().stream().map(String::toLowerCase).collect(Collectors.toSet())
                : new HashSet<>();
        note.setUserTags(userTags);

        // 2️⃣ Generate auto tags
        List<String> autoTags = tfidfService.generateTagsForNoteContent(dto.getContent(), 6);

        // 3️⃣ Merge user tags with auto tags for the "all tags" set
        List<String> merged = tfidfService.mergeUserTags(autoTags, new ArrayList<>(userTags));
        note.setTags(new HashSet<>(merged));

        return repo.save(note);
    }

    public Note update(Long id, NoteDto dto) {
        Note note = repo.findById(id).orElseThrow(NoSuchElementException::new);

        note.setTitle(dto.getTitle());
        note.setContent(dto.getContent());
        note.setLastModifiedDate(LocalDateTime.now());

        // preprocess tokens
        note.setTokens(new HashSet<>(textProcessing.preprocess(dto.getContent())));

        // generate tags
        List<String> autoTags = tfidfService.generateTagsForNoteContent(dto.getContent(), 6);

        // save user tags separately first
        Set<String> userTagsSet = dto.getUserTags() != null
                ? new HashSet<>(dto.getUserTags().stream().map(String::toLowerCase).toList())
                : new HashSet<>();
        note.setUserTags(userTagsSet);

        // merge auto tags with user tags to form final tags
        List<String> merged = tfidfService.mergeUserTags(autoTags, new ArrayList<>(userTagsSet));
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

    // ---------------- Updated Search Method ----------------
    public List<NoteResponse> search(String q, String tag) {
        List<Note> notes = repo.findAll();

        // Preprocess query tokens for TF-IDF
        Set<String> queryTokens = (q != null && !q.isEmpty())
                ? new HashSet<>(textProcessing.preprocess(q))
                : Collections.emptySet();

        // Compute IDF map for corpus
        Map<String, Integer> df = tfidfService.computeDfAll();
        int N = (int) repo.count();
        Map<String, Double> idf = tfidfService.computeIdf(df, Math.max(N, 1));

        // Filter notes by token overlap or tag if provided
        notes = notes.stream().filter(n -> {
            boolean matchesQuery = queryTokens.isEmpty()
                    || (n.getTokens() != null && !Collections.disjoint(n.getTokens(), queryTokens));
            boolean matchesTag = tag == null || tag.isEmpty()
                    || (n.getTags() != null && n.getTags().stream().anyMatch(t -> t.contains(tag)))
                    || (n.getUserTags() != null && n.getUserTags().stream().anyMatch(t -> t.contains(tag)));
            return matchesQuery && matchesTag;
        }).toList();

        // Compute relevance score for each note
        List<NoteResult> scoredNotes = notes.stream()
                .map(n -> {
                    double score = tfidfService.computeRelevanceScore(
                            new ArrayList<>(n.getTokens()),
                            new ArrayList<>(queryTokens),
                            idf);
                    System.out.println(n.getTitle() + " : right here por favor " + score); // debug
                    return new NoteResult(n, score);
                })
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .toList();

        // Convert to DTO
        return scoredNotes.stream()
                .map(r -> toDto(r.note))
                .collect(Collectors.toList());
    }

    private static class NoteResult {
        public Note note;
        public double score;

        public NoteResult(Note note, double score) {
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

        // Merge all tags for response
        dto.setAllTags(
                Stream.concat(
                        note.getTags() != null ? note.getTags().stream() : Stream.empty(),
                        note.getUserTags() != null ? note.getUserTags().stream() : Stream.empty())
                        .collect(Collectors.toList()));

        // separate lists
        dto.setTags(note.getTags() != null ? new ArrayList<>(note.getTags()) : new ArrayList<>());
        dto.setUserTags(note.getUserTags() != null ? new ArrayList<>(note.getUserTags()) : new ArrayList<>());

        return dto;
    }
}
