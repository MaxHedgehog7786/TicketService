package ru.ticketservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.ticketservice.service.*;

import java.util.List;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final EventService eventService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<EventDto>> list(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(eventService.getFavorites(userService.resolveId(ud)));
    }

    @PostMapping("/{eventId}")
    public ResponseEntity<Void> add(@PathVariable Integer eventId,
                                    @AuthenticationPrincipal UserDetails ud) {
        eventService.addFavorite(userService.resolveId(ud), eventId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> remove(@PathVariable Integer eventId,
                                       @AuthenticationPrincipal UserDetails ud) {
        eventService.removeFavorite(userService.resolveId(ud), eventId);
        return ResponseEntity.noContent().build();
    }
}
