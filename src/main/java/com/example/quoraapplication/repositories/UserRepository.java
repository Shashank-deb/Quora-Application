package com.example.quoraapplication.repositories;

import com.example.quoraapplication.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.followedTags WHERE u.id = :id")
    Optional<User> findByIdWithTags(@Param("id") Long id);
}