package com.menstrualtracker.symptom.service;
import com.menstrualtracker.common.dto.ApiResponse;
import com.menstrualtracker.common.exception.BusinessException;
import com.menstrualtracker.symptom.dto.SymptomTagDTO;
import com.menstrualtracker.symptom.dto.SymptomTagRequest;
import com.menstrualtracker.symptom.entity.SymptomTag;
import com.menstrualtracker.symptom.repository.SymptomTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class SymptomTagService {
    private final SymptomTagRepository symptomTagRepository;
    public ApiResponse<List<SymptomTagDTO>> getTags(Long userId) {
        return ApiResponse.success(symptomTagRepository.findByUserIdAndDeletedFalseOrderBySortOrderAsc(userId)
                .stream().map(this::toDTO).collect(Collectors.toList()));
    }
    public ApiResponse<List<SymptomTagDTO>> getTagsByType(Long userId, String type) {
        return ApiResponse.success(symptomTagRepository.findByUserIdAndTypeAndDeletedFalse(userId, type)
                .stream().map(this::toDTO).collect(Collectors.toList()));
    }
    @Transactional
    public ApiResponse<SymptomTagDTO> createTag(Long userId, SymptomTagRequest request) {
        if (symptomTagRepository.existsByUserIdAndNameAndTypeAndDeletedFalse(userId, request.getName(), request.getType()))
            throw BusinessException.conflict("Tag already exists");
        SymptomTag tag = SymptomTag.builder()
                .userId(userId).name(request.getName()).type(request.getType())
                .color(request.getColor() != null ? request.getColor() : "#409EFF")
                .icon(request.getIcon()).sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0).build();
        tag = symptomTagRepository.save(tag);
        return ApiResponse.success("Tag created", toDTO(tag));
    }
    @Transactional
    public ApiResponse<Void> deleteTag(Long userId, Long tagId) {
        SymptomTag tag = symptomTagRepository.findByIdAndUserIdAndDeletedFalse(tagId, userId)
                .orElseThrow(() -> BusinessException.notFound("Tag not found"));
        tag.setDeleted(true);
        symptomTagRepository.save(tag);
        return ApiResponse.success("Tag deleted", null);
    }
    private SymptomTagDTO toDTO(SymptomTag tag) {
        return SymptomTagDTO.builder().id(tag.getId()).userId(tag.getUserId()).name(tag.getName())
                .type(tag.getType()).color(tag.getColor()).icon(tag.getIcon()).sortOrder(tag.getSortOrder()).build();
    }
}