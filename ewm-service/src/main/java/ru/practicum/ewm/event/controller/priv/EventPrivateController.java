package ru.practicum.ewm.event.controller.priv;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import ru.practicum.ewm.dto.ewm_service.event.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.ewm_service.event.EventShortDto;
import ru.practicum.ewm.dto.ewm_service.event.NewEventDto;
import ru.practicum.ewm.dto.ewm_service.participation.ParticipationRequestDto;
import ru.practicum.ewm.dto.ewm_service.event.update_event.UpdateEventUserRequest;
import ru.practicum.ewm.dto.exception.BadRequestException;
import ru.practicum.ewm.dto.exception.ConflictException;
import ru.practicum.ewm.event.service.EventService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/users/{userId}/events")
@Validated
public class EventPrivateController {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEventsByUserId(@PathVariable @Positive Long userId,
                                                 @RequestParam(defaultValue = "0") @Min(0) int from,
                                                 @RequestParam(defaultValue = "10") @Positive int size) {
        return eventService.getEventByUserId(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable @Positive Long userId,
                                    @RequestBody @Valid NewEventDto newEventDto) {

        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            log.error("Дата и время на которые намечено событие не может быть раньше, " +
                    "чем через два часа от текущего момента");
            throw new ConflictException("Дата и время на которые намечено событие не может быть раньше, " +
                    "чем через два часа от текущего момента");
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
        if (updateEventUserRequest.getEventDate() != null
                && updateEventUserRequest.getEventDate().isBefore(LocalDateTime.now())) {
            log.error("Дата и время на которые намечено событие не может быть в прошлом.");
            throw new BadRequestException("Дата и время на которые намечено событие не может быть в прошлом.");
        }

        if (updateEventUserRequest.getEventDate() != null
                && updateEventUserRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            log.error("Дата и время на которые намечено событие не может быть раньше, " +
                    "чем через два часа от текущего момента");
            throw new ConflictException("Дата и время на которые намечено событие не может быть раньше, " +
                    "чем через два часа от текущего момента");
        }

        return eventService.updateEventByUserIdAndEventId(userId, eventId, updateEventUserRequest);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getEventParticipationListByEventId(@PathVariable @Positive Long userId,
                                                                            @PathVariable @Positive Long eventId) {
        return eventService.getEventParticipationListByEventId(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateEventRequestStatusByEventId(@PathVariable @Positive Long userId,
                                                                            @PathVariable @Positive Long eventId,
                                                                            @RequestBody @Valid EventRequestStatusUpdateRequest request) {
        return eventService.updateEventRequestStatusByEventId(userId, eventId, request);
    }
}
