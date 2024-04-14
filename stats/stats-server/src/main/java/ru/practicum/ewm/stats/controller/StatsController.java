package ru.practicum.ewm.stats.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.dto.stats.EndpointHitDto;
import ru.practicum.ewm.stats.service.StatsService;

@AllArgsConstructor
@RestController
@RequestMapping
public class StatsController {
    private final StatsService statsService;

    @PostMapping
    public EndpointHitDto hitStats(@RequestBody EndpointHitDto endpointHitDto) {
        return null;
    }
}
