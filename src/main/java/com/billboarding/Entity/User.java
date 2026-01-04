package com.billboarding.Entity;

import com.billboarding.ENUM.KycStatus;
import com.billboarding.ENUM.TwoFactorMethod;
import com.billboarding.ENUM.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * User entity implementing Spring Security UserDetails
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_role", columnList = "role"),
    @Index(name = "idx_user_kyc", columnList = "kycStatus"),
    @Index(name = "idx_user_blocked", columnList = "blocked")
})
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min=3, max=60)
    @Column(nullable = false, length = 60)
    private String name;

    @NotBlank(message = "Email is required")
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;

    @Size(min = 10, max = 15)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;  // ADMIN / OWNER / ADVERTISER

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KycStatus kycStatus;

    @Column(nullable = false)
    private boolean blocked;

    @Column(nullable = false)
    private boolean twoFactorEnabled;

    @Column(nullable = false)
    private boolean forceTwoFactor;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TwoFactorMethod twoFactorMethod = TwoFactorMethod.NONE;
    // ===============================
    //      USERDETAILS METHODS
    // ===============================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return this.email;   // we use email as username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // no expiration logic
    }

    @Override
    public boolean isAccountNonLocked() {
        return !blocked; // blocked users cannot log in
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // passwords never expire by default
    }

    @Override
    public boolean isEnabled() {
        return true; // user always enabled
    }
}
