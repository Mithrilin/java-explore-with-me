package ru.practicum.ewm.event.service;

import ru.practicum.ewm.dto.ewm_service.event.EventFullDto;
import ru.practicum.ewm.dto.ewm_service.event.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.ewm_service.event.EventShortDto;
import ru.practicum.ewm.dto.ewm_service.event.NewEventDto;
import ru.practicum.ewm.dto.ewm_service.event.ParticipationRequestDto;
import ru.practicum.ewm.dto.ewm_service.event.UpdateEventAdminRequest;
import ru.practicum.ewm.dto.ewm_service.event.UpdateEventUserRequest;
import ru.practicum.ewm.dto.ewm_service.event.params.FullEventRequestParams;
import ru.practicum.ewm.dto.ewm_service.event.params.ShortEventRequestParams;

import java.util.List;

public interface EventService {

    EventFullDto addEvent(Long userId, NewEventDto newEventDto);

    List<EventFullDto> getEventsByParams(FullEventRequestParams fullEventRequestParams);

    EventFullDto updateEventAdminRequestByEventId(Long eventId, UpdateEventAdminRequest request);

    List<EventShortDto> getEventsBySearchWithParams(ShortEventRequestParams shortEventRequestParams);

    EventFullDto getEventById(Long eventId);

    List<EventShortDto> getEventByUserId(Long userId, int from, int size);

    EventFullDto getEventByUserIdAndEventId(Long userId, Long eventId);

    EventFullDto updateEventByUserIdAndEventId(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    List<ParticipationRequestDto> getEventParticipationListByEventId(Long userId, Long eventId);

    EventFullDto updateEventRequestStatusByEventId(Long userId, Long eventId, EventRequestStatusUpdateRequest request);
}
