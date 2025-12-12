package com.example.information_retrieval_backend.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.information_retrieval_backend.model.Note;
import com.example.information_retrieval_backend.repository.NoteRepository;

@Service
public class TfidfService {

    private final NoteRepository noteRepository;
    private final TextProcessingService textProcessingService;

    public TfidfService(NoteRepository noteRepo, TextProcessingService tp) {
        this.noteRepository = noteRepo;
        this.textProcessingService = tp;
    }

    public double computeRelevanceScore(List<String> noteTokens, List<String> queryTokens, Map<String, Double> idfMap) {
    double score = 0.0;
    Map<String, Integer> tf = computeTf(noteTokens);
    for (String token : queryTokens) {
        double idf = idfMap.getOrDefault(token, Math.log(1.0));
        score += tf.getOrDefault(token, 0) * idf;
    }
    return score;
}


    // Compute TF for a single document
    public Map<String, Integer> computeTf(List<String> tokens) {
        Map<String, Integer> tf = new HashMap<>();
        for (String t : tokens) tf.merge(t, 1, Integer::sum);
        return tf;
    }

    // Compute document frequencies across corpus
    public Map<String, Integer> computeDfAll() {
        List<Note> all = noteRepository.findAll();
        Map<String, Integer> df = new HashMap<>();
        for (Note n : all) {
            // tokens stored in entity; if not stored, preprocess(n.getContent())
            Set<String> unique = n.getTokens();
            for (String t : unique) df.merge(t, 1, Integer::sum);
        }
        return df;
    }

    // IDF: log(N / df)
    public Map<String, Double> computeIdf(Map<String,Integer> df, int N) {
        Map<String, Double> idf = new HashMap<>();
        df.forEach((token, count) -> {
            idf.put(token, Math.log((double)N / (double)count));
        });
        return idf;
    }

    // Compute TF-IDF scores for a doc
    public List<Map.Entry<String, Double>> computeTfidf(List<String> tokens, Map<String, Double> idfMap) {
        Map<String, Integer> tf = computeTf(tokens);
        Map<String, Double> scores = new HashMap<>();
        tf.forEach((token, freq) -> {
            double idf = idfMap.getOrDefault(token, Math.log(1.0)); // fallback
            scores.put(token, freq * idf);
        });
        return scores.entrySet()
                     .stream()
                     .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                     .collect(Collectors.toList());
    }

    // Generate top N tags for a single document
    public List<String> generateTagsForNoteContent(String content, int topN) {
        List<String> tokens = textProcessingService.preprocess(content);

        // compute IDF from entire corpus
        Map<String, Integer> df = computeDfAll();
        int N = (int) noteRepository.count(); // implement count query
        Map<String, Double> idf = computeIdf(df, Math.max(N, 1));

        List<Map.Entry<String, Double>> tfidf = computeTfidf(tokens, idf);

        return tfidf.stream()
                    .limit(topN)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
    }

    // Merge with user tags, boosting them
    public List<String> mergeUserTags(List<String> autoTags, List<String> userTags) {
        LinkedHashSet<String> merged = new LinkedHashSet<>();
        // add user tags first (higher priority)
        if (userTags != null) userTags.forEach(t -> merged.add(t.toLowerCase()));
        // then auto tags
        autoTags.forEach(t -> merged.add(t.toLowerCase()));
        return new ArrayList<>(merged);
    }
}

