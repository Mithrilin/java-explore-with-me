package ru.practicum.ewm.stats.service;

import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.dto.stats.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    EndpointHit addHitStats(EndpointHit endpointHit);

    List<ViewStats> getViewStatsListWithoutUris(LocalDateTime start, LocalDateTime end, Boolean unique);

    List<ViewStats> getViewStatsListWithUris(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}
