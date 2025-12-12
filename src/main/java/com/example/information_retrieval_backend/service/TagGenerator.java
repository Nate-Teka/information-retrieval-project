// package com.example.information_retrieval_backend.service;

// import java.util.*;
// import java.util.stream.Collectors;
// import org.apache.commons.lang3.StringUtils;
// import opennlp.tools.stemmer.PorterStemmer;

// public class TagGenerator {

//     private static final Set<String> STOP_WORDS = Set.of(
//         "a", "an", "the", "and", "or", "is", "in", "on", "of", "for", "to", "with"
//         // add more stop words as needed
//     );

//     public static String generateTags(String content) {
//         if (StringUtils.isBlank(content)) return "";

//         // 1. Tokenize
//         String[] tokens = content.toLowerCase().replaceAll("[^a-z0-9\\s]", "").split("\\s+");

//         // 2. Remove stop words
//         List<String> filtered = Arrays.stream(tokens)
//                 .filter(t -> !STOP_WORDS.contains(t))
//                 .collect(Collectors.toList());

//         // 3. Lemmatization / Stemming
//         PorterStemmer stemmer = new PorterStemmer();
//         List<String> stemmed = filtered.stream()
//                 .map(stemmer::stem)
//                 .collect(Collectors.toList());

//         // 4. Count frequency and pick top 5-10 terms
//         Map<String, Long> freq = stemmed.stream()
//                 .collect(Collectors.groupingBy(w -> w, Collectors.counting()));

//         return freq.entrySet().stream()
//                 .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
//                 .limit(5) // choose top 5 terms as tags
//                 .map(Map.Entry::getKey)
//                 .collect(Collectors.joining(","));
//     }
// }
