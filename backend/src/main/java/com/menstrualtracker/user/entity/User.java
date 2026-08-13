package com.menstrualtracker.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted = 1 WHERE id = ?")
@SQLRestriction("deleted = 0")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "open_id", length = 64, unique = true)
    private String openId;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 500)
    private String avatar;

    @Column(length = 50)
    private String nickname;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String role = "USER";

    @Column(name = "login_type", nullable = false, length = 20)
    @Builder.Default
    private String loginType = "PASSWORD";

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "password_change_required", nullable = false)
    @Builder.Default
    private Boolean passwordChangeRequired = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
