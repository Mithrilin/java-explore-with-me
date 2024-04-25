package ru.practicum.ewm.user.controller.priv;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.dto.ewm_service.event.EventFullDto;
import ru.practicum.ewm.dto.ewm_service.event.ParticipationRequestDto;
import ru.practicum.ewm.user.service.UserService;

import javax.validation.constraints.Positive;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/users/{userId}/requests")
@Validated
public class UserPrivateController {
    private final UserService userService;

    @GetMapping
    public List<ParticipationRequestDto> getRequestsByUserId(@PathVariable @Positive long userId) {
        return userService.getRequestsByUserId(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addParticipation(@PathVariable @Positive long userId,
                                                    @RequestParam @Positive long eventId) {
        return userService.addParticipation(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public EventFullDto cancelParticipationByUserId(@PathVariable @Positive long userId,
                                                    @PathVariable @Positive long requestId) {
        return userService.cancelParticipationByUserId(userId, requestId);
    }
}
