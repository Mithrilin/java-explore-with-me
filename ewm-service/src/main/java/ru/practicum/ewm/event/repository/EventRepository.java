package ru.practicum.ewm.event.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.ewm.event.model.Event;

import java.util.List;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    Set<Event> findByIdIn(Set<Long> eventIds);

    List<Event> findByInitiator_Id(Long userId, PageRequest pageRequest);
}
