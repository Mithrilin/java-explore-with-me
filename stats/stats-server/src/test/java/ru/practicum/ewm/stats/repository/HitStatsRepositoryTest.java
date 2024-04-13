package ru.practicum.ewm.stats.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.stats.model.HitStats;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class HitStatsRepositoryTest {
    private HitStats hitStats_1;
    private HitStats hitStats_2;
    private HitStats hitStats_3;
    private HitStats hitStats_4;
    private HitStats hitStats_5;
    private HitStats hitStats_6;
    private List<HitStats> hitStatsList;
    private LocalDateTime start;
    private LocalDateTime end;

    @Autowired
    private HitStatsRepository hitStatsRepository;

    @BeforeEach
    void setUp() {
        hitStats_1 = new HitStats(null, "ewm-main-service", "/events", "121.0.0.1",
                LocalDateTime.of(2025, 4, 19, 9, 54, 34));
        hitStats_2 = new HitStats(null, "ewm-main-service", "/events", "121.0.0.1",
                LocalDateTime.of(2025, 4, 19, 9, 54, 44));
        hitStats_3 = new HitStats(null, "ewm-main-service", "/events", "122.2.2.1",
                LocalDateTime.of(2025, 4, 19, 9, 54, 34));
        hitStats_4 = new HitStats(null, "ewm-main-service", "/events/5", "122.2.2.1",
                LocalDateTime.of(2025, 4, 19, 9, 54, 44));
        hitStats_5 = new HitStats(null, "ewm-main-service", "/events/2", "121.0.0.1",
                LocalDateTime.of(2025, 4, 19, 9, 54, 34));
        hitStats_6 = new HitStats(null, "ewm-main-service", "/events", "121.0.0.1",
                LocalDateTime.of(2035, 4, 19, 9, 54, 34));
        hitStatsList = List.of(hitStats_1, hitStats_2, hitStats_3, hitStats_4, hitStats_5, hitStats_6);
        start = LocalDateTime.of(2020, 1, 1, 1, 1, 1);
        end = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
    }

    @Test
    @DisplayName("Получение списка статистики без Uris, когда Unique = True.")
    void getViewStatsWithUniqueTrue_whenWithoutUrisAndUniqueTrue_thenReturnedViewStatsList() {
        hitStatsRepository.saveAll(hitStatsList);

        List<ViewStats> viewStatsList = hitStatsRepository.getViewStatsWithUniqueTrue(start, end);

        assertEquals(3, viewStatsList.size());
        assertEquals(2, viewStatsList.get(0).getHits());
        assertEquals(hitStats_1.getUri(), viewStatsList.get(0).getUri());
    }

    @Test
    @DisplayName("Получение списка статистики без Uris, когда Unique = False.")
    void getViewStatsWithUniqueFalse_whenWithoutUrisAndUniqueFalse_thenReturnedViewStatsList() {
        hitStatsRepository.saveAll(hitStatsList);

        List<ViewStats> viewStatsList = hitStatsRepository.getViewStatsWithUniqueFalse(start, end);

        assertEquals(3, viewStatsList.size());
        assertEquals(3, viewStatsList.get(0).getHits());
        assertEquals(hitStats_1.getUri(), viewStatsList.get(0).getUri());
    }

    @Test
    @DisplayName("Получение списка статистики с Uris, когда Unique = True.")
    void getViewStatsWithUrisUniqueTrue() {
        List<String> uris = List.of("/events", "/events/5");
        hitStatsRepository.saveAll(hitStatsList);

        List<ViewStats> viewStatsList = hitStatsRepository.getViewStatsWithUrisUniqueTrue(uris, start, end);

        assertEquals(2, viewStatsList.size());
        assertEquals(2, viewStatsList.get(0).getHits());
        assertEquals(hitStats_1.getUri(), viewStatsList.get(0).getUri());
        assertEquals(hitStats_4.getUri(), viewStatsList.get(1).getUri());
    }

    @Test
    @DisplayName("Получение списка статистики с Uris, когда Unique = False.")
    void getViewStatsWithUrisUniqueFalse() {
        List<String> uris = List.of("/events", "/events/5");
        hitStatsRepository.saveAll(hitStatsList);

        List<ViewStats> viewStatsList = hitStatsRepository.getViewStatsWithUrisUniqueFalse(uris, start, end);

        assertEquals(2, viewStatsList.size());
        assertEquals(3, viewStatsList.get(0).getHits());
        assertEquals(hitStats_1.getUri(), viewStatsList.get(0).getUri());
        assertEquals(hitStats_4.getUri(), viewStatsList.get(1).getUri());
    }
}