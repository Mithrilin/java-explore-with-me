package ru.practicum.ewm.mark.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.mark.model.Mark;

import java.util.List;
import java.util.Optional;

public interface MarkRepository  extends JpaRepository<Mark, Long> {

    Optional<Mark> findByUser_IdAndEvent_Id(Long userId, Long eventId);

    @Query(value = "SELECT CAST(sum(mark_value) AS DECIMAL(3,1))/CAST(count(events_id) AS DECIMAL(3,1)) AS rating_count " +
            "FROM marks AS m " +
            "WHERE m.events_id = :eventId " +
            "GROUP BY events_id " +
            "ORDER BY rating_count DESC", nativeQuery = true)
    Double getRatingByEventId(Long eventId);

    @Query(value =
            "SELECT * " +
            "FROM marks AS m1 " +
            "WHERE m1.user_id IN (SELECT m2.user_id " +
                                 "FROM marks AS m2 " +
                                 "WHERE m2.events_id IN (SELECT m3.events_id " +
                                                        "FROM marks AS m3 " +
                                                        "WHERE m3.user_id = :userId))", nativeQuery = true)
    List<Mark> findAllMarksOfSimilarUsers(long userId);
}
