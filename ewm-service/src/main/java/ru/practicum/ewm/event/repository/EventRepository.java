package ru.practicum.ewm.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.dto.ewm_service.event.enums.EventLifecycleStates;
import ru.practicum.ewm.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    Set<Event> findByIdIn(Set<Long> eventIds);

    List<Event> findByInitiator_Id(Long userId, PageRequest pageRequest);

    Page<Event> findByStateLike(EventLifecycleStates eventLifecycleStates, PageRequest pageRequest);

    @Query(value =
            "SELECT * " +
            "FROM events AS e " +
            "WHERE e.id IN (SELECT p1.event_id " +
                           "FROM participation AS p1 " +
                           "WHERE NOT p1.event_id IN (SELECT p2.event_id " +
                                                     "FROM participation AS p2 " +
                                                     "WHERE p2.requester_id = :requesterId) " +
                           "AND p1.requester_id = :userIdWithMinDiff) " +
            "AND e.event_date > :now", nativeQuery = true)
    List<Event> findAllByRequesterIdAndUserId(Long requesterId, Long userIdWithMinDiff, LocalDateTime now);

    @Query(value =
            "SELECT * " +
            "FROM events AS e " +
            "LEFT JOIN (SELECT u.id AS user_id, sum(e.rating)*0.1 AS user_rating " +
                       "FROM users AS u " +
                       "LEFT JOIN events AS e ON u.id = e.initiator_id " +
                       "GROUP BY u.id " +
                       "ORDER BY user_rating DESC NULLS LAST) AS j ON e.initiator_id = j.user_id " +
            "WHERE NOT e.id IN (SELECT p1.event_id " +
                               "FROM participation AS p1 " +
                               "WHERE p1.event_id = :requesterId) " +
            "AND e.state LIKE 'PUBLISHED' " +
            "AND e.event_date > :now " +
            "ORDER BY j.user_rating DESC NULLS LAST " +
            "LIMIT :minNumberOfRecommendations", nativeQuery = true)
    List<Event> findAllByUserRating(long requesterId, LocalDateTime now, int minNumberOfRecommendations);
}
