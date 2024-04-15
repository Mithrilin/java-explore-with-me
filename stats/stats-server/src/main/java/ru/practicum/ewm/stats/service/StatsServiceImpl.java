package ru.practicum.ewm.stats.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.stats.mapper.HitStatsMapper;
import ru.practicum.ewm.stats.model.HitStats;
import ru.practicum.ewm.stats.repository.HitStatsRepository;

import javax.transaction.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final HitStatsRepository hitStatsRepository;
    private final HitStatsMapper hitStatsMapper;

    @Override
    @Transactional
    public EndpointHit addHitStats(EndpointHit endpointHit) {
        HitStats hitStats = hitStatsMapper.toHitStats(endpointHit);
        HitStats returnedHitStats = hitStatsRepository.save(hitStats);
        EndpointHit returnedEndpointHit = hitStatsMapper.toEndpointHit(returnedHitStats);
        log.info("Добавлена новая запись в статистику запросов с ID = {}", returnedHitStats.getId());
        return returnedEndpointHit;
    }
}
