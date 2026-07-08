package com.example.storage.repository;

import com.example.storage.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserName(String userName);

    @Query("""
            SELECT DISTINCT u FROM User u
            LEFT JOIN FETCH u.files
            """)
    List<User> findAllWithDetails();
}
