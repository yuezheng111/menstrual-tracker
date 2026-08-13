package com.menstrualtracker.admin.service;

import com.menstrualtracker.admin.dto.AdminUserDTO;
import com.menstrualtracker.common.cache.CacheService;
import com.menstrualtracker.common.exception.BusinessException;
import com.menstrualtracker.record.repository.MenstrualRecordRepository;
import com.menstrualtracker.user.entity.User;
import com.menstrualtracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final MenstrualRecordRepository recordRepository;
    private final PasswordEncoder passwordEncoder;
    private final CacheService cacheService;

    public Page<AdminUserDTO> listUsers(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        Page<User> users;
        if (keyword != null && !keyword.isBlank()) {
            users = userRepository.findByUsernameContaining(keyword, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        return users.map(this::toDTO);
    }

    @Transactional
    public AdminUserDTO setEnabled(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("User not found"));
        user.setEnabled(enabled);
        userRepository.save(user);
        if (!enabled) {
            // 禁用后使其现有会话立即失效
            cacheService.deleteUserSessions(userId);
        }
        return toDTO(user);
    }

    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        // 重置密码后使现有会话失效
        cacheService.deleteUserSessions(userId);
    }

    private AdminUserDTO toDTO(User user) {
        long recordCount = recordRepository.countByUserId(user.getId());
        return AdminUserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .recordCount(recordCount)
                .createdAt(user.getCreatedAt())
                .build();
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }
}
