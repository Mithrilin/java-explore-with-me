package ru.practicum.ewm.event.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.ewm_service.event.EventFullDto;
import ru.practicum.ewm.dto.ewm_service.event.NewEventDto;

@Slf4j
@Service
@AllArgsConstructor
public class EventServiceImpl implements EventService {

    @Override
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {

        return null;
    }
}
