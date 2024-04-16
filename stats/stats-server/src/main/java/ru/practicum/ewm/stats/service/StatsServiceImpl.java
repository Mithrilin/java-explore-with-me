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
//        List<HitStats> hitStatsList;
//        if (unique) {
//            hitStatsList = hitStatsRepository.findDistinctByUriAndApiAndTimestampBetween(start, end);
//        } else {
//            hitStatsList = hitStatsRepository.findByTimestampBetween(start, end);
//        }
        List<HitStats> hitStatsList = hitStatsRepository.findByTimestampBetween(start, end);
        if (hitStatsList.isEmpty()) {
            log.info("Список со статистикой просмотров для указанных параметров пуст.");
            return new ArrayList<>();
        }


//        log.info("со статистикой просмотров размером {} возвращён.", pageRequestParams.getSize());
        return null;
    }

    @Override
    public List<ViewStats> getViewStatsListWithUris(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        List<HitStats> hitStatsList = hitStatsRepository.findByUriInAndTimestampBetween(uris, start, end);
        if (hitStatsList.isEmpty()) {
            log.info("Список со статистикой просмотров для указанных параметров пуст.");
            return new ArrayList<>();
        }

//        log.info("со статистикой просмотров размером {} возвращён.", pageRequestParams.getSize());
        return null;
    }
}
