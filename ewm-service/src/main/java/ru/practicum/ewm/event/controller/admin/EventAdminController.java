package ru.practicum.ewm.event.controller.admin;

import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.dto.ewm_service.event.EventFullDto;
import ru.practicum.ewm.dto.ewm_service.event.update_event.UpdateEventAdminRequest;
import ru.practicum.ewm.dto.ewm_service.event.enums.EventLifecycleStates;
import ru.practicum.ewm.dto.ewm_service.event.params.FullEventRequestParams;
import ru.practicum.ewm.event.service.EventService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/admin/events")
@Validated
public class EventAdminController {
    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> getEventsByParams(@RequestParam(required = false) List<Long> users,
                                                @RequestParam(required = false) List<EventLifecycleStates> states,
                                                @RequestParam(required = false) List<Long> categories,
                                                @RequestParam(required = false)
                                                @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                LocalDateTime rangeStart,
                                                @RequestParam(required = false)
                                                @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                LocalDateTime rangeEnd,
                                                @RequestParam(defaultValue = "0") @Min(0) int from,
                                                @RequestParam(defaultValue = "10") @Positive int size) {
        FullEventRequestParams fullEventRequestParams = new FullEventRequestParams(
                users,
                states,
                categories,
                rangeStart,
                rangeEnd,
                from,
                size
        );
        return eventService.getEventsByParams(fullEventRequestParams);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventAdminRequestByEventId(@PathVariable @Positive Long eventId,
                                                         @RequestBody @Valid UpdateEventAdminRequest request) {
        return eventService.updateEventAdminRequestByEventId(eventId, request);
    }
}
