package com.example.information_retrieval_backend.model;

import com.example.information_retrieval_backend.model.Note;
import java.util.Map;

public class ScoredNote {
    private final Note note;
    private final double score;
    private final Map<String, Double> scoreBreakdown; // optional

    public ScoredNote(Note note, double score, Map<String, Double> scoreBreakdown) {
        this.note = note;
        this.score = score;
        this.scoreBreakdown = scoreBreakdown;
    }

    public Note getNote() {
        return note;
    }

    public double getScore() {
        return score;
    }

    public Map<String, Double> getScoreBreakdown() {
        return scoreBreakdown;
    }
}
