package com.filmytell.support.repository;

import com.filmytell.support.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.active = true")
    List<User> findAllActiveUsers();

    @Query("""
        SELECT u FROM User u
        JOIN u.roles r
        WHERE r.name = :roleName AND u.active = true
    """)
    List<User> findActiveUsersByRoleName(com.filmytell.support.enums.RoleName roleName);
}
