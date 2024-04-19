package ru.practicum.ewm.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.stats.model.HitStats;

import java.time.LocalDateTime;
import java.util.List;

public interface HitStatsRepository extends JpaRepository<HitStats, Long> {

    @Query(value = "SELECT new ru.practicum.ewm.dto.stats.ViewStats (h.app, h.uri, COUNT(DISTINCT h.ip)) " +
            "FROM HitStats AS h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<ViewStats> getViewStatsWithUniqueTrue(LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT new ru.practicum.ewm.dto.stats.ViewStats (h.app, h.uri, COUNT(h.ip)) " +
            "FROM HitStats AS h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(h.ip) DESC")
    List<ViewStats> getViewStatsWithUniqueFalse(LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT new ru.practicum.ewm.dto.stats.ViewStats (h.app, h.uri, COUNT(DISTINCT h.ip)) " +
            "FROM HitStats AS h " +
            "WHERE h.uri IN :uris " +
            "AND h.timestamp BETWEEN :start AND :end " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<ViewStats> getViewStatsWithUrisUniqueTrue(List<String> uris, LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT new ru.practicum.ewm.dto.stats.ViewStats (h.app, h.uri, COUNT(h.ip)) " +
            "FROM HitStats AS h " +
            "WHERE h.uri IN :uris " +
            "AND h.timestamp BETWEEN :start AND :end " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(h.ip) DESC")
    List<ViewStats> getViewStatsWithUrisUniqueFalse(List<String> uris, LocalDateTime start, LocalDateTime end);
}
