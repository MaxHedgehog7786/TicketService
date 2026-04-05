package ru.ticketservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.ticketservice.service.*;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final AuthService authService;
    private final UserService userService;

    @PutMapping
    public ResponseEntity<?> update(@RequestBody UpdateProfileRequest req,
                                    @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(authService.updateProfile(userService.resolveId(ud), req));
    }

    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(@AuthenticationPrincipal UserDetails ud) {
        authService.updateProfile(userService.resolveId(ud),
                new UpdateProfileRequest(null, null, null, true));
        return ResponseEntity.ok().build();
    }
}
