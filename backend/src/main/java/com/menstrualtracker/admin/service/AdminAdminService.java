package com.menstrualtracker.admin.service;

import com.menstrualtracker.common.exception.BusinessException;
import com.menstrualtracker.user.entity.User;
import com.menstrualtracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminAdminService {

    private final UserRepository userRepository;

    public List<User> listAdmins() {
        return userRepository.findByRole("ADMIN");
    }

    @Transactional
    public User grantAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("User not found"));
        user.setRole("ADMIN");
        return userRepository.save(user);
    }

    @Transactional
    public User revokeAdmin(Long userId, Long operatorId) {
        if (userId.equals(operatorId)) {
            throw BusinessException.badRequest("You cannot revoke your own admin role");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("User not found"));
        if (!"ADMIN".equals(user.getRole())) {
            throw BusinessException.badRequest("User is not an admin");
        }
        user.setRole("USER");
        return userRepository.save(user);
    }
}
