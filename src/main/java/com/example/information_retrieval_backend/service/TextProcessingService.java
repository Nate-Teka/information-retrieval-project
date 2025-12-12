package com.example.information_retrieval_backend.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.springframework.stereotype.Service;

import edu.stanford.nlp.pipeline.Annotation;


import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

@Service
public class TextProcessingService {

    private final StanfordCoreNLP pipeline;
    private final CharArraySet stopwords;

    public TextProcessingService() {
        // init CoreNLP pipeline (tokenize, ssplit, pos, lemma)
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma");
        // don't load heavy models repeatedly; reuse pipeline
        this.pipeline = new StanfordCoreNLP(props);

        // use Lucene English stopwords
        this.stopwords = EnglishAnalyzer.getDefaultStopSet();
    }

    public List<String> preprocess(String text) {
        if (text == null) return Collections.emptyList();

        // normalization: lowercase, remove non-alphanum (keeps spaces)
        String normalized = text.toLowerCase().replaceAll("[^a-z0-9\\s]", " ");

        // CoreNLP annotation
        Annotation doc = new Annotation(normalized);
        pipeline.annotate(doc);

        List<String> lemmas = new ArrayList<>();
        for (CoreMap sentence : doc.get(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                if (lemma == null) continue;
                lemma = lemma.trim().toLowerCase();
                // filter stopwords and short tokens
                if (lemma.length() > 1 && !stopwords.contains(lemma)) {
                    lemmas.add(lemma);
                }
            }
        }
        return lemmas;
    }
}

