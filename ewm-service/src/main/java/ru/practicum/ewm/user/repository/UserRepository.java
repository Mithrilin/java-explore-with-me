package ru.practicum.ewm.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.user.model.User;

import java.util.List;
import java.util.Map;

public interface UserRepository extends JpaRepository<User, Long> {

    Page<User> findByIdIn(List<Long> ids, PageRequest pageRequest);

    @Query(value = "SELECT u.id, SUM(e.rating)*0.1 AS user_rating " +
            "FROM users AS u " +
            "LEFT JOIN events AS e ON u.id = e.initiator_id " +
            "WHERE u.id IN :userIdList " +
            "GROUP BY u.id " +
            "ORDER BY user_rating DESC NULLS LAST", nativeQuery = true)
    List<Map<String, Object>> findAllRatingsByUserIdIn(List<Long> userIdList);

    @Query(value = "SELECT SUM(e.rating)*0.1 AS user_rating " +
            "FROM users AS u " +
            "LEFT JOIN events AS e ON u.id = e.initiator_id " +
            "WHERE u.id = :userId " +
            "GROUP BY u.id ", nativeQuery = true)
    Double getRatingByUserId(Long userId);

    @Query(value = "SELECT u.id, u.email, u.name " +
            "FROM users AS u " +
            "LEFT JOIN events AS e ON u.id = e.initiator_id " +
            "GROUP BY u.id " +
            "ORDER BY SUM(e.rating)*0.1 DESC NULLS LAST", nativeQuery = true)
    Page<User> findAllWithRatingSort(PageRequest pageRequest);
}
