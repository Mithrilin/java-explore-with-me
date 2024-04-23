package ru.practicum.ewm.event.controller.priv;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.dto.ewm_service.event.EventFullDto;
import ru.practicum.ewm.dto.ewm_service.event.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.ewm_service.event.EventShortDto;
import ru.practicum.ewm.dto.ewm_service.event.NewEventDto;
import ru.practicum.ewm.dto.ewm_service.event.ParticipationRequestDto;
import ru.practicum.ewm.dto.ewm_service.event.UpdateEventUserRequest;
import ru.practicum.ewm.dto.ewm_service.event.enums.UpdateEventStates;
import ru.practicum.ewm.event.service.EventService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/users/{userId}/events")
@Validated
public class EventPrivateController {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEventByUserId(@PathVariable @Positive Long userId,
                                                @RequestParam(defaultValue = "0") @Min(0) int from,
                                                @RequestParam(defaultValue = "10") @Positive int size) {
        return eventService.getEventByUserId(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable @Positive Long userId,
                                    @RequestBody @Valid NewEventDto newEventDto) {
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            // временное решение
            throw new RuntimeException();
        }
        return eventService.addEvent(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventByUserIdAndEventId(@PathVariable @Positive Long userId,
                                                   @PathVariable @Positive Long eventId) {
        return eventService.getEventByUserIdAndEventId(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByUserIdAndEventId(@PathVariable @Positive Long userId,
                                                      @PathVariable @Positive Long eventId,
                                                      @RequestBody @Valid UpdateEventUserRequest updateEventUserRequest) {
        if (updateEventUserRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))
                || (updateEventUserRequest.getStateAction() != UpdateEventStates.REJECT_EVENT
                && updateEventUserRequest.getStateAction() != UpdateEventStates.SEND_TO_REVIEW)) {
            // временное решение
            throw new RuntimeException();
        }
        return eventService.updateEventByUserIdAndEventId(userId, eventId, updateEventUserRequest);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getEventParticipationList(@PathVariable @Positive Long userId,
                                                                   @PathVariable @Positive Long eventId) {
        return eventService.getEventParticipationList(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventFullDto updateEventRequestStatus(@PathVariable @Positive Long userId,
                                                 @PathVariable @Positive Long eventId,
                                                 @RequestBody @Valid EventRequestStatusUpdateRequest request) {
        return eventService.updateEventRequestStatus(userId, eventId, request);
    }
}
