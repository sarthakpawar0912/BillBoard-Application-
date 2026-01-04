package com.billboarding.Controller.AUTH;

import com.billboarding.DTO.UpdateEmailRequest;
import com.billboarding.Entity.User;
import com.billboarding.Services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    // 🔐 Admin update email
    @PutMapping("/{id}/email")
    public ResponseEntity<User> updateUserEmail(
            @PathVariable Long id,
            @RequestBody @Valid UpdateEmailRequest req
    ) {
        return ResponseEntity.ok(
                userService.adminUpdateEmail(id, req.getNewEmail())
        );
    }
}
