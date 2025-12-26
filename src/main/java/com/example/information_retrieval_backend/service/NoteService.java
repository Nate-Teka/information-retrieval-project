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

    public Note create(NoteDto dto) {
        Note note = new Note();
        note.setTitle(dto.getTitle());
        note.setContent(dto.getContent());
        note.setCreatedDate(LocalDateTime.now());
        note.setLastModifiedDate(LocalDateTime.now());

        Set<String> titleTokens = new HashSet<>(textProcessing.preprocess(dto.getTitle(), ""));
        Set<String> contentTokens = new HashSet<>(textProcessing.preprocess("", dto.getContent()));
        Set<String> allTokens = new HashSet<>(titleTokens);
        allTokens.addAll(contentTokens);

        note.setTitleTokens(titleTokens);
        note.setContentTokens(contentTokens);
        note.setTokens(allTokens);

        Set<String> userTags = dto.getUserTags() != null
                ? dto.getUserTags().stream().map(String::toLowerCase).collect(Collectors.toSet())
                : new HashSet<>();
        note.setUserTags(userTags);

        List<String> autoTags = tfidfService.generateTagsForNoteContent(dto.getTitle(), dto.getContent(), 6);

        List<String> mergedTags = tfidfService.mergeUserTags(autoTags, new ArrayList<>(userTags));
        note.setTags(new HashSet<>(mergedTags));

        return repo.save(note);
    }

    public Note update(Long id, NoteDto dto) {
        Note note = repo.findById(id).orElseThrow(NoSuchElementException::new);

        note.setTitle(dto.getTitle());
        note.setContent(dto.getContent());
        note.setLastModifiedDate(LocalDateTime.now());

        Set<String> titleTokens = new HashSet<>(textProcessing.preprocess(dto.getTitle(), ""));
        Set<String> contentTokens = new HashSet<>(textProcessing.preprocess("", dto.getContent()));
        Set<String> allTokens = new HashSet<>(titleTokens);
        allTokens.addAll(contentTokens);

        note.setTitleTokens(titleTokens);
        note.setContentTokens(contentTokens);
        note.setTokens(allTokens);

        Set<String> userTags = dto.getUserTags() != null
                ? dto.getUserTags().stream().map(String::toLowerCase).collect(Collectors.toSet())
                : new HashSet<>();
        note.setUserTags(userTags);

        List<String> autoTags = tfidfService.generateTagsForNoteContent(dto.getTitle(), dto.getContent(), 6);
        List<String> mergedTags = tfidfService.mergeUserTags(autoTags, new ArrayList<>(userTags));
        note.setTags(new HashSet<>(mergedTags));

        return repo.save(note);
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) throw new NoSuchElementException();
        repo.deleteById(id);
    }

    public List<Note> findAll() {
        return repo.findAll();
    }

    public List<NoteResponse> search(String q, String tag) {
        List<Note> notes = repo.findAll();

        if ((q == null || q.isEmpty()) && (tag == null || tag.isEmpty())) {
            return notes.stream().map(this::toDtoPublic).toList();
        }

        List<String> queryTokens = (q != null && !q.isEmpty())
                ? textProcessing.preprocess("", q)
                : Collections.emptyList();

        // IDF right here
        Map<String, Integer> df = tfidfService.computeDfAll();
        int N = Math.max((int) repo.count(), 1);
        Map<String, Double> idf = tfidfService.computeIdf(df, N);

        Map<String, Double> queryVector =
                tfidfService.buildTfidfVector(queryTokens, idf);

        List<NoteResult> ranked = notes.stream()
                .map(note -> {

                    if (tag != null && !tag.isEmpty()) {
                        boolean match =
                                (note.getTags() != null && note.getTags().contains(tag)) ||
                                (note.getUserTags() != null && note.getUserTags().contains(tag));
                        if (!match) return null;
                    }

                    Map<String, Double> docVector =
                            tfidfService.buildWeightedDocumentVector(note, idf);

                    double score =
                            tfidfService.cosineSimilarity(docVector, queryVector);

                    return score > 0 ? new NoteResult(note, score) : null;
                })
                .filter(Objects::nonNull)
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .toList();

        return ranked.stream()
                .map(r -> toDtoPublic(r.note))
                .toList();
    }
    private static class NoteResult {
        public Note note;
        public double score;

        public NoteResult(Note note, double score) {
            this.note = note;
            this.score = score;
        }
    }

    private NoteResponse toDto(Note note) {
        NoteResponse dto = new NoteResponse();
        dto.setId(note.getId());
        dto.setTitle(note.getTitle());
        dto.setContent(note.getContent());
        dto.setLastUpdatedAt(note.getLastModifiedDate());

        dto.setUserTags(note.getUserTags() != null ? new ArrayList<>(note.getUserTags()) : new ArrayList<>());

        dto.setTags(new ArrayList<>());
        return dto;
    }

    public NoteResponse toDtoPublic(Note note) {
        return toDto(note);
    }
}
