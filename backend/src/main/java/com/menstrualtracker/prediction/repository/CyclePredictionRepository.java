package com.menstrualtracker.prediction.repository;
import com.menstrualtracker.prediction.entity.CyclePrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface CyclePredictionRepository extends JpaRepository<CyclePrediction, Long> {
    Optional<CyclePrediction> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}