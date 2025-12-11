package com.example.information_retrieval_backend.repository;

import com.example.information_retrieval_backend.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {

    // Simple tag filter: finds notes where tags contain the tag fragment (case-insensitive)
    @Query("SELECT n FROM Note n WHERE LOWER(n.tags) LIKE LOWER(CONCAT('%', :tag, '%'))")
    List<Note> findByTag(@Param("tag") String tag);

    /**
     * Full-Text Search using MySQL MATCH ... AGAINST
     * This uses a native query because JPA JPQL doesn't support MATCH.
     * It returns rows ordered by relevance descending.
     *
     * The query expects the table to have a FULLTEXT index on (title, content).
     */
    @Query(value = "SELECT *, MATCH(title, content) AGAINST (:q IN NATURAL LANGUAGE MODE) AS relevance " +
                   "FROM notes " +
                   "WHERE MATCH(title, content) AGAINST (:q IN NATURAL LANGUAGE MODE) " +
                   "ORDER BY relevance DESC",
           nativeQuery = true)
    List<Note> searchByFullText(@Param("q") String q);
}