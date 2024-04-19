package ru.practicum.ewm.stats.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.stats.mapper.HitStatsMapper;
import ru.practicum.ewm.stats.model.HitStats;
import ru.practicum.ewm.stats.repository.HitStatsRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    public List<ViewStats> getViewStatsListWithoutUris(LocalDateTime start, LocalDateTime end, Boolean unique) {
        List<ViewStats> viewStatsList;
        if (unique) {
            viewStatsList = hitStatsRepository.getViewStatsWithUniqueTrue(start, end);
        } else {
            viewStatsList = hitStatsRepository.getViewStatsWithUniqueFalse(start, end);
        }
        if (viewStatsList.isEmpty()) {
            log.info("Список со статистикой просмотров для указанных параметров пуст.");
            return new ArrayList<>();
        }
        log.info("Список статистики просмотров с параметром unique = {} размером {} возвращён.", unique, viewStatsList.size());
        return viewStatsList;
    }

    @Override
    public List<ViewStats> getViewStatsListWithUris(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        List<ViewStats> viewStatsList;
        if (unique) {
            viewStatsList = hitStatsRepository.getViewStatsWithUrisUniqueTrue(uris, start, end);
        } else {
            viewStatsList = hitStatsRepository.getViewStatsWithUrisUniqueFalse(uris, start, end);
        }
        if (viewStatsList.isEmpty()) {
            log.info("Список со статистикой просмотров для указанных параметров пуст.");
            return new ArrayList<>();
        }
        log.info("Список статистики просмотров с параметром unique = {} размером {} возвращён.", unique, viewStatsList.size());
        return viewStatsList;
    }
}
