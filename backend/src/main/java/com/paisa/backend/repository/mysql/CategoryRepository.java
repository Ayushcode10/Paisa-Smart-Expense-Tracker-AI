package com.paisa.backend.repository.mysql;

import com.paisa.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Fetch system defaults (userId = null) + user's custom ones
    // This single query powers the GET /api/categories endpoint
    @Query("""
    SELECT c
    FROM Category c
    WHERE c.userId IS NULL
       OR c.userId = :userId
""")
    List<Category> findAllForUser(@org.springframework.data.repository.query.Param("userId") Long userId);


    //used when deleting - ensure that it belongs to the user and is not default
    Optional<Category> findByNameAndUserId(String name, Long userId);

    //used at startup, to check if defaults already exists
    boolean existsByIsDefaultTrue();
}
