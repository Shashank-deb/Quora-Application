package com.example.quoraapplication.repositories;

import com.example.quoraapplication.models.Answer;
import com.example.quoraapplication.models.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Page<Answer> findByQuestionId(Long questionId, Pageable pageable);

}
