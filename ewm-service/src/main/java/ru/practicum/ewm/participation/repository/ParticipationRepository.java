package ru.practicum.ewm.participation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.participation.model.Participation;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {

    Optional<Participation> findByEvent_IdAndRequester_Id(Long eventId, Long userId);

    List<Participation> findByRequester_Id(Long userId);

    List<Participation> findByEvent_Id(Long eventId);

    List<Participation> findByIdIn(List<Long> requestIds);
}
