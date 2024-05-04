package ru.practicum.ewm.event.controller.publ;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.dto.ewm_service.event.EventFullDto;
import ru.practicum.ewm.dto.ewm_service.event.EventShortDto;
import ru.practicum.ewm.dto.ewm_service.event.enums.EventSortAvailableValues;
import ru.practicum.ewm.dto.ewm_service.event.params.ShortEventRequestParams;
import ru.practicum.ewm.dto.exception.BadRequestException;
import ru.practicum.ewm.event.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/events")
@Validated
public class EventPublicController {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEventsBySearchWithParams(@RequestParam(required = false) String text,
                                                           @RequestParam(required = false) List<Long> categories,
                                                           @RequestParam(required = false) Boolean paid,
                                                           @RequestParam(required = false)
                                                           @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                           LocalDateTime rangeStart,
                                                           @RequestParam(required = false)
                                                           @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                           LocalDateTime rangeEnd,
                                                           @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                                           @RequestParam(required = false) EventSortAvailableValues sort,
                                                           @RequestParam(defaultValue = "0") @Min(0) int from,
                                                           @RequestParam(defaultValue = "10") @Positive int size,
                                                           HttpServletRequest request) {

        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            log.error("Конец диапазона дат событий не может быть раньше начала.");
            throw new BadRequestException("Конец диапазона дат событий не может быть раньше начала.");
        }

        ShortEventRequestParams shortEventRequestParams = new ShortEventRequestParams(
                text,
                categories,
                paid,
                rangeStart,
                rangeEnd,
                onlyAvailable,
                sort,
                from,
                size
        );
        return eventService.getEventsBySearchWithParams(shortEventRequestParams, request);
    }

    @GetMapping("/{id}")
    public EventFullDto getEventById(@PathVariable @Positive Long id, HttpServletRequest request) {
        return eventService.getEventById(id, request);
    }
}
