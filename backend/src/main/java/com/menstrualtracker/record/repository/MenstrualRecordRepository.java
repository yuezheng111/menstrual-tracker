package com.menstrualtracker.record.repository;
import com.menstrualtracker.record.entity.MenstrualRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Repository
public interface MenstrualRecordRepository extends JpaRepository<MenstrualRecord, Long> {
    Page<MenstrualRecord> findByUserIdAndDeletedFalseOrderByStartDateDesc(Long userId, Pageable pageable);
    List<MenstrualRecord> findByUserIdAndDeletedFalseOrderByStartDateDesc(Long userId);
    Optional<MenstrualRecord> findByIdAndUserIdAndDeletedFalse(Long id, Long userId);
    @Query("SELECT r FROM MenstrualRecord r WHERE r.userId = :userId AND r.deleted = false "
           + "AND FUNCTION('YEAR', r.startDate) = :year ORDER BY r.startDate DESC")
    List<MenstrualRecord> findByUserIdAndYear(@Param("userId") Long userId, @Param("year") int year);
    @Query("SELECT r FROM MenstrualRecord r WHERE r.userId = :userId AND r.deleted = false "
           + "AND FUNCTION('YEAR', r.startDate) = :year AND FUNCTION('MONTH', r.startDate) = :month ORDER BY r.startDate DESC")
    List<MenstrualRecord> findByUserIdAndYearAndMonth(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);
    @Query("SELECT r FROM MenstrualRecord r WHERE r.userId = :userId AND r.deleted = false "
           + "AND r.startDate >= :start AND r.startDate <= :end ORDER BY r.startDate ASC")
    List<MenstrualRecord> findByUserIdAndDateRange(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    long countByUserId(Long userId);

    long countByCreatedAtAfter(java.time.LocalDateTime time);

    List<MenstrualRecord> findByCreatedAtAfter(java.time.LocalDateTime time);

    @Query(value = "SELECT COUNT(DISTINCT user_id) FROM menstrual_records WHERE created_at >= :since AND deleted = 0", nativeQuery = true)
    long countActiveUsersSince(@Param("since") java.time.LocalDateTime since);

    @Query(value = "SELECT symptoms, COUNT(*) FROM menstrual_records WHERE symptoms IS NOT NULL AND symptoms != '' AND deleted = 0 GROUP BY symptoms ORDER BY COUNT(*) DESC LIMIT :limit", nativeQuery = true)
    java.util.List<Object[]> findTopSymptoms(@Param("limit") int limit);

        @Query("SELECT r FROM MenstrualRecord r WHERE r.userId = :userId AND r.deleted = false "
           + "AND FUNCTION('YEAR', r.startDate) = :year ORDER BY r.startDate DESC")
    Page<MenstrualRecord> findByUserIdAndYear(@Param("userId") Long userId, @Param("year") int year, Pageable pageable);

        @Query("SELECT r FROM MenstrualRecord r WHERE r.userId = :userId AND r.deleted = false "
           + "AND FUNCTION('YEAR', r.startDate) = :year AND FUNCTION('MONTH', r.startDate) = :month ORDER BY r.startDate DESC")
    Page<MenstrualRecord> findByUserIdAndYearAndMonth(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month, Pageable pageable);
}
