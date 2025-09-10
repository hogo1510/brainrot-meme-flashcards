package org.lucas.brainrotmemeflashcards.repo;


import org.lucas.brainrotmemeflashcards.model.Category;
import org.lucas.brainrotmemeflashcards.model.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FlashcardRepo extends JpaRepository<Flashcard, Long> {
    List<Flashcard> findByCategory(Category category);
    List<Flashcard> findByCategoryId(Long categoryId);
}
