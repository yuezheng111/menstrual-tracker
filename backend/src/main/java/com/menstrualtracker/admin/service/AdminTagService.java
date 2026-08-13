package com.menstrualtracker.admin.service;

import com.menstrualtracker.common.exception.BusinessException;
import com.menstrualtracker.symptom.entity.SymptomTag;
import com.menstrualtracker.symptom.repository.SymptomTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminTagService {

    private final SymptomTagRepository symptomTagRepository;

    public List<SymptomTag> listAll(String type) {
        if (type != null && !type.isBlank()) {
            return symptomTagRepository.findByType(type);
        }
        return symptomTagRepository.findAll();
    }

    @Transactional
    public void delete(Long tagId) {
        if (!symptomTagRepository.existsById(tagId)) {
            throw BusinessException.notFound("Tag not found");
        }
        symptomTagRepository.deleteById(tagId);
    }
}
