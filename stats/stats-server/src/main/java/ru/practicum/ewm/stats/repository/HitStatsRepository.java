package ru.practicum.ewm.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.stats.model.HitStats;

public interface HitStatsRepository extends JpaRepository<HitStats, Long> {
}
