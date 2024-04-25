package ru.practicum.ewm.event.controller.publ;

import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.dto.ewm_service.event.EventFullDto;
import ru.practicum.ewm.dto.ewm_service.event.EventShortDto;
import ru.practicum.ewm.dto.ewm_service.event.params.ShortEventRequestParams;
import ru.practicum.ewm.event.service.EventService;

import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/events")
@Validated
public class EventPublicController {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEventsBySearchWithParams(@RequestParam String text,
                                                           @RequestParam List<Long> categories,
                                                           @RequestParam Boolean paid,
                                                           @RequestParam
                                                           @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                           LocalDateTime rangeStart,
                                                           @RequestParam
                                                           @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                           LocalDateTime rangeEnd,
                                                           @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                                           @RequestParam String sort,  // Available values : EVENT_DATE, VIEWS
                                                           @RequestParam(defaultValue = "0") @Min(0) int from,
                                                           @RequestParam(defaultValue = "10") @Positive int size) {
        ShortEventRequestParams shortEventRequestParams = new ShortEventRequestParams(
                text,
                categories,
                paid,
                rangeStart,
                rangeEnd,
                onlyAvailable,
                from,
                size
        );
        return eventService.getEventsBySearchWithParams(shortEventRequestParams);
    }

    @GetMapping("/{id}")
    public EventFullDto getEventById(@PathVariable @Positive Long id) {
        return eventService.getEventById(id);
    }
}
