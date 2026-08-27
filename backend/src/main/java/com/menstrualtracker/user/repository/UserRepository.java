package com.menstrualtracker.user.repository;
import com.menstrualtracker.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<User> findByOpenId(String openId);

    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR (u.nickname IS NOT NULL AND LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<User> searchByUsernameOrNickname(@Param("keyword") String keyword, Pageable pageable);

    List<User> findByRole(String role);

    long countByCreatedAtAfter(LocalDateTime time);

    long countByRole(String role);

    @Query("SELECT FUNCTION('DATE', u.createdAt) AS d, COUNT(u) FROM User u WHERE u.createdAt >= :since GROUP BY FUNCTION('DATE', u.createdAt) ORDER BY d")
    java.util.List<Object[]> countUsersByDay(@Param("since") java.time.LocalDateTime since);
}
