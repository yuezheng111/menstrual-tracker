package com.menstrualtracker.symptom.repository;
import com.menstrualtracker.symptom.entity.SymptomTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface SymptomTagRepository extends JpaRepository<SymptomTag, Long> {
    List<SymptomTag> findByUserIdAndTypeAndDeletedFalse(Long userId, String type);
    List<SymptomTag> findByUserIdAndDeletedFalseOrderBySortOrderAsc(Long userId);
    Optional<SymptomTag> findByIdAndUserIdAndDeletedFalse(Long id, Long userId);
    boolean existsByUserIdAndNameAndTypeAndDeletedFalse(Long userId, String name, String type);

    List<SymptomTag> findByType(String type);
}