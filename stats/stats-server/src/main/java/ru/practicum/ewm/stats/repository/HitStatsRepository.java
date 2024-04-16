package ru.practicum.ewm.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.stats.model.HitStats;

import java.time.LocalDateTime;
import java.util.List;

public interface HitStatsRepository extends JpaRepository<HitStats, Long> {

//    @Query(value = "select it from Item as it " +
//            "where it.available = true " +
//            "and (UPPER(it.name) like UPPER(concat('%', ?1, '%')) " +
//            "or UPPER(it.description) like UPPER(concat('%', ?1, '%')))")
//    List<HitStats> findDistinctByUriAndApiAndTimestampBetween(LocalDateTime start, LocalDateTime end);

    List<HitStats> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    List<HitStats> findByUriInAndTimestampBetween(List<String> uris, LocalDateTime start, LocalDateTime end);
}
