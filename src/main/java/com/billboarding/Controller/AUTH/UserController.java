package com.billboarding.Controller.AUTH;

import com.billboarding.DTO.UpdateEmailRequest;
import com.billboarding.Entity.User;
import com.billboarding.Services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 🔄 Update own email
    @PutMapping("/email")
    public ResponseEntity<User> updateOwnEmail(
            @RequestBody @Valid UpdateEmailRequest req,
            Authentication auth
    ) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(
                userService.updateEmail(user, req.getNewEmail())
        );
    }
}
