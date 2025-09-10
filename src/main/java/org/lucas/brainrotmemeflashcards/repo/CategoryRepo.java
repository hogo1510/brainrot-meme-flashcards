package org.lucas.brainrotmemeflashcards.repo;

import org.lucas.brainrotmemeflashcards.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepo extends JpaRepository<Category, Long> {
}
