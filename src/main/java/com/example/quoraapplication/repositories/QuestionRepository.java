package com.example.quoraapplication.repositories;

import com.example.quoraapplication.models.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT q FROM Question q JOIN q.tags t where t.id IN :tagIds")
    Page<Question> findQuestionByTags(Set<Long> tagIds, Pageable pageable);
}
