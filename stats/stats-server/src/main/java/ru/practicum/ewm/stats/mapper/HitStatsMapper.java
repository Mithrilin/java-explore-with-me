package ru.practicum.ewm.stats.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.stats.model.HitStats;

@Mapper(componentModel = "spring")
public interface HitStatsMapper {

    HitStats endpointHitToHitStats(EndpointHit endpointHit);

    EndpointHit hitStatsToEndpointHit(HitStats returnedHitStats);
}
