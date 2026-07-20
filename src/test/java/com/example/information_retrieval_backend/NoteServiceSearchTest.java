package com.example.information_retrieval_backend;

import com.example.information_retrieval_backend.dto.NoteResponse;
import com.example.information_retrieval_backend.model.Note;
import com.example.information_retrieval_backend.repository.NoteRepository;
import com.example.information_retrieval_backend.service.NoteService;
import com.example.information_retrieval_backend.service.TextProcessingService;
import com.example.information_retrieval_backend.service.TfidfService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NoteServiceSearchTest {

    @Test
    void searchByTagShouldMatchCaseInsensitivelyAndReturnResultsForTagOnlyQuery() {
        NoteRepository repo = mock(NoteRepository.class);
        TextProcessingService textProcessing = new TextProcessingService();
        TfidfService tfidfService = new TfidfService(repo, textProcessing);
        NoteService noteService = new NoteService(repo, textProcessing, tfidfService);

        Note matchingNote = new Note();
        matchingNote.setId(1L);
        matchingNote.setTitle("Java notes");
        matchingNote.setContent("Useful Java content");
        matchingNote.setTags(Set.of("java"));
        matchingNote.setUserTags(Set.of());

        when(repo.findAll()).thenReturn(List.of(matchingNote));
        when(repo.count()).thenReturn(1L);

        List<NoteResponse> results = noteService.search("", "Java");

        assertEquals(1, results.size());
        assertEquals("Java notes", results.get(0).getTitle());
    }
}
