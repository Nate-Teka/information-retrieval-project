package com.example.information_retrieval_backend.service;

import com.example.information_retrieval_backend.model.Note;
import com.example.information_retrieval_backend.repository.NoteRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TfidfService {

    private final NoteRepository noteRepository;
    private final TextProcessingService textProcessingService;

    public TfidfService(NoteRepository noteRepo, TextProcessingService tp) {
        this.noteRepository = noteRepo;
        this.textProcessingService = tp;
    }

    // ---------- BASIC TF / DF / IDF ----------

    public Map<String, Integer> computeTf(List<String> tokens) {
        Map<String, Integer> tf = new HashMap<>();
        for (String t : tokens) {
            tf.merge(t, 1, Integer::sum);
        }
        return tf;
    }

    public Map<String, Integer> computeDfAll() {
        List<Note> all = noteRepository.findAll();
        Map<String, Integer> df = new HashMap<>();

        for (Note n : all) {
            Set<String> unique = n.getTokens();
            for (String t : unique) {
                df.merge(t, 1, Integer::sum);
            }
        }
        return df;
    }

    public Map<String, Double> computeIdf(Map<String, Integer> df, int N) {
        Map<String, Double> idf = new HashMap<>();
        df.forEach((token, count) -> idf.put(token, Math.log((double) N / (double) count)));
        return idf;
    }

    // ---------- QUERY VECTOR ----------

    public Map<String, Double> buildTfidfVector(
            List<String> tokens,
            Map<String, Double> idfMap) {
        Map<String, Integer> tf = computeTf(tokens);
        Map<String, Double> vector = new HashMap<>();

        tf.forEach((term, freq) -> {
            double idf = idfMap.getOrDefault(term, Math.log(1.0));
            vector.put(term, freq * idf);
        });

        return vector;
    }

    // ---------- DOCUMENT VECTOR (FIELD-WEIGHTED) ----------

    public Map<String, Double> buildWeightedDocumentVector(
            Note note,
            Map<String, Double> idfMap) {
        Map<String, Double> vector = new HashMap<>();

        final double USER_TAG_WEIGHT = 3.0;
        final double TITLE_WEIGHT = 2.0;
        final double CONTENT_WEIGHT = 1.0;

        // content (lowest priority)
        if (note.getContentTokens() != null) {
            for (String t : note.getContentTokens()) {
                double idf = idfMap.getOrDefault(t, Math.log(1.0));
                vector.merge(t, CONTENT_WEIGHT * idf, Double::sum);
            }
        }

        // title (medium priority)
        if (note.getTitleTokens() != null) {
            for (String t : note.getTitleTokens()) {
                double idf = idfMap.getOrDefault(t, Math.log(1.0));
                vector.merge(t, TITLE_WEIGHT * idf, Double::sum);
            }
        }

        // user tags (highest priority)
        if (note.getUserTags() != null) {
            for (String t : note.getUserTags()) {
                double idf = idfMap.getOrDefault(t, Math.log(1.0));
                vector.merge(t, USER_TAG_WEIGHT * idf, Double::sum);
            }
        }

        return vector;
    }

    // ---------- COSINE SIMILARITY ----------

    public double cosineSimilarity(
            Map<String, Double> docVector,
            Map<String, Double> queryVector) {
        double dot = 0.0;
        double docNorm = 0.0;
        double queryNorm = 0.0;

        for (Map.Entry<String, Double> e : queryVector.entrySet()) {
            double q = e.getValue();
            double d = docVector.getOrDefault(e.getKey(), 0.0);
            dot += q * d;
        }

        for (double v : docVector.values()) {
            docNorm += v * v;
        }

        for (double v : queryVector.values()) {
            queryNorm += v * v;
        }

        if (docNorm == 0 || queryNorm == 0)
            return 0.0;

        return dot / (Math.sqrt(docNorm) * Math.sqrt(queryNorm));
    }

    // ---------- AUTO TAG GENERATION (UNCHANGED) ----------

    public List<Map.Entry<String, Double>> computeTfidf(
            List<String> tokens,
            Map<String, Double> idfMap) {
        Map<String, Integer> tf = computeTf(tokens);
        Map<String, Double> scores = new HashMap<>();

        tf.forEach((token, freq) -> {
            double idf = idfMap.getOrDefault(token, Math.log(1.0));
            scores.put(token, freq * idf);
        });

        return scores.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toList());
    }

    public List<String> generateTagsForNoteContent(
            String title,
            String content,
            int topN) {
        List<String> tokens = textProcessingService.preprocess(title, content);
        Map<String, Integer> df = computeDfAll();
        int N = Math.max((int) noteRepository.count(), 1);
        Map<String, Double> idf = computeIdf(df, N);

        return computeTfidf(tokens, idf)
                .stream()
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public List<String> mergeUserTags(List<String> autoTags, List<String> userTags) {
        LinkedHashSet<String> merged = new LinkedHashSet<>();
        if (userTags != null)
            userTags.forEach(t -> merged.add(t.toLowerCase()));
        autoTags.forEach(t -> merged.add(t.toLowerCase()));
        return new ArrayList<>(merged);
    }

    public void logScoreBreakdown(
            Note note,
            Map<String, Double> idf,
            Map<String, Double> queryVector) {
        System.out.println("\nNOTE ID: " + note.getId());
        System.out.println("Title: " + note.getTitle());

        Map<String, Double> docVector = buildWeightedDocumentVector(note, idf);

        double dot = 0.0;

        for (String term : queryVector.keySet()) {
            double q = queryVector.get(term);
            double d = docVector.getOrDefault(term, 0.0);
            double contrib = q * d;

            if (contrib > 0) {
                System.out.printf(
                        "  term='%s'  query=%.3f  doc=%.3f  contrib=%.3f%n",
                        term, q, d, contrib);
                dot += contrib;
            }
        }

        double sim = cosineSimilarity(docVector, queryVector);
        System.out.printf("  FINAL COSINE SCORE = %.4f%n", sim);
    }

}
