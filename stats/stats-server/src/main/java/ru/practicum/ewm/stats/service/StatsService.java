package ru.practicum.ewm.stats.service;

import ru.practicum.ewm.dto.stats.EndpointHit;

public interface StatsService {

    EndpointHit addHitStats(EndpointHit endpointHit);
}
