package ru.practicum.ewm.stats.controller;

import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.dto.exception.NotValidException;
import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.stats.service.StatsService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping
@Validated
public class StatsController {
    private final StatsService statsService;

    @PostMapping("/hit")
    public EndpointHit addHitStats(@RequestBody @Valid EndpointHit endpointHit) {
        return statsService.addHitStats(endpointHit);
    }

    @GetMapping("/stats")
    public List<ViewStats> getViewStatsList(@RequestParam
                                            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                            LocalDateTime start,
                                            @RequestParam
                                            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                            LocalDateTime end,
                                            @RequestParam (required = false) List<String> uris,
                                            @RequestParam (defaultValue = "false") Boolean unique) {
        if (start.isAfter(end)) {
            throw new NotValidException("Дата начала должна быть раньше даты конца.");
        }
        if (uris == null) {
            return statsService.getViewStatsListWithoutUris(start, end, unique);
        } else {
            return statsService.getViewStatsListWithUris(start, end, uris, unique);
        }
    }
}
