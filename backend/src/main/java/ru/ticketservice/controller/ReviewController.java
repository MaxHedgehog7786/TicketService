package ru.ticketservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.ticketservice.service.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;

    @GetMapping("/{eventId}")
    public ResponseEntity<List<ReviewDto>> list(@PathVariable Integer eventId) {
        return ResponseEntity.ok(reviewService.getByEvent(eventId));
    }

    @PostMapping
    public ResponseEntity<ReviewDto> add(@RequestBody ReviewRequest req,
                                         @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.addReview(userService.resolveId(ud),
                        req.getEventId(), req.getRating(), req.getComment()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewDto> update(@PathVariable Integer id,
                                             @RequestBody ReviewRequest req,
                                             @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(reviewService.updateReview(id, userService.resolveId(ud),
                req.getRating(), req.getComment()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id,
                                        @AuthenticationPrincipal UserDetails ud) {
        reviewService.deleteReview(id, userService.resolveId(ud));
        return ResponseEntity.noContent().build();
    }
}
