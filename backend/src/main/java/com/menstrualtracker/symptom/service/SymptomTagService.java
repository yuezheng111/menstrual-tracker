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

    private static final String[] DEFAULT_SYMPTOMS = {
            "头痛", "疲劳", "腹胀", "腹痛", "背痛",
            "恶心", "头晕", "长痘", "乳房胀痛", "失眠"
    };

    private static final String[] DEFAULT_EMOTIONS = {
            "开心", "难过", "焦虑", "易怒", "平静",
            "精力充沛", "情绪化", "疲惫", "专注", "压力大"
    };

    private final SymptomTagRepository symptomTagRepository;
    public ApiResponse<List<SymptomTagDTO>> getTags(Long userId) {
        return ApiResponse.success(symptomTagRepository.findByUserIdAndDeletedFalseOrderBySortOrderAsc(userId)
                .stream().map(this::toDTO).collect(Collectors.toList()));
    }
    public ApiResponse<List<SymptomTagDTO>> getTagsByType(Long userId, String type) {
        List<SymptomTag> tags = symptomTagRepository.findByUserIdAndTypeAndDeletedFalse(userId, type);
        if (tags.isEmpty()) {
            seedDefaultTags(userId, type);
            tags = symptomTagRepository.findByUserIdAndTypeAndDeletedFalse(userId, type);
        }
        return ApiResponse.success(tags.stream()
                .map(this::toDTO)
                .collect(Collectors.toList()));
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

    // First-time users get the same default tag set that was previously hardcoded in the client.
    private void seedDefaultTags(Long userId, String type) {
        String[] names;
        if ("SYMPTOM".equals(type)) {
            names = DEFAULT_SYMPTOMS;
        } else if ("EMOTION".equals(type)) {
            names = DEFAULT_EMOTIONS;
        } else {
            return;
        }
        for (int i = 0; i < names.length; i++) {
            symptomTagRepository.save(SymptomTag.builder()
                    .userId(userId)
                    .name(names[i])
                    .type(type)
                    .sortOrder(i)
                    .build());
        }
    }
}
