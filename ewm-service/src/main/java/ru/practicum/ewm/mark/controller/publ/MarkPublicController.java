package ru.practicum.ewm.mark.controller.publ;

import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.dto.ewm_service.event.EventShortDto;
import ru.practicum.ewm.dto.ewm_service.user.UserDto;
import ru.practicum.ewm.mark.service.MarkService;

import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping
@Validated
public class MarkPublicController {
    private final MarkService markService;

    @GetMapping("/events/rating")
    public List<EventShortDto> getEventRating(@RequestParam(defaultValue = "0") @Min(0) int from,
                                              @RequestParam(defaultValue = "10") @Positive int size) {
        return markService.getEventRating(from, size);
    }

    @GetMapping("/users/rating")
    public List<UserDto> getUserRating(@RequestParam(defaultValue = "0") @Min(0) int from,
                                       @RequestParam(defaultValue = "10") @Positive int size) {
        return markService.getUserRating(from, size);
    }

    @GetMapping("/events/recommendations/{userId}")
    public List<EventShortDto> getRecommendations(@PathVariable @Positive long userId,
                                                  @RequestParam(defaultValue = "0") @Min(0) int from,
                                                  @RequestParam(defaultValue = "10") @Positive int size) {
        return markService.getRecommendations(userId, from, size);
    }
}
