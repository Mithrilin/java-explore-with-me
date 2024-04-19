package ru.practicum.ewm.event.service;

import ru.practicum.ewm.dto.ewm_service.event.EventFullDto;
import ru.practicum.ewm.dto.ewm_service.event.NewEventDto;

public interface EventService {

    EventFullDto addEvent(Long userId, NewEventDto newEventDto);
}
