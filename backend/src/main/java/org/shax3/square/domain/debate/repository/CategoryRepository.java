package org.shax3.square.domain.debate.repository;

import org.shax3.square.domain.debate.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByName(String name);
}
