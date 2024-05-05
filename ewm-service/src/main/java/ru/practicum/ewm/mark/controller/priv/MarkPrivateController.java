package ru.practicum.ewm.mark.controller.priv;

import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.mark.service.MarkService;

import javax.validation.constraints.Max;
import javax.validation.constraints.Positive;

@AllArgsConstructor
@RestController
@RequestMapping("/users/{userId}/events/{eventId}/marks")
@Validated
public class MarkPrivateController {
    private final MarkService markService;

    @PostMapping("/{markValue}")
    public void addMark(@PathVariable @Positive long userId,
                        @PathVariable @Positive long eventId,
                        @PathVariable @Positive @Max(10) int markValue) {
        markService.addMark(userId, eventId, markValue);
    }

    @DeleteMapping
    public void deleteMark(@PathVariable @Positive long userId,
                           @PathVariable @Positive long eventId) {
        markService.deleteMark(userId, eventId);
    }

    @PatchMapping("/{markValue}")
    public void updateMark(@PathVariable @Positive long userId,
                           @PathVariable @Positive long eventId,
                           @PathVariable @Positive @Max(10) int markValue) {
        markService.updateMark(userId, eventId, markValue);
    }
}
