package ru.practicum.ewm.event.service;

import ru.practicum.ewm.dto.ewm_service.event.EventFullDto;
import ru.practicum.ewm.dto.ewm_service.event.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.ewm_service.event.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.ewm_service.event.EventShortDto;
import ru.practicum.ewm.dto.ewm_service.event.NewEventDto;
import ru.practicum.ewm.dto.ewm_service.participation.ParticipationRequestDto;
import ru.practicum.ewm.dto.ewm_service.event.update_event.UpdateEventAdminRequest;
import ru.practicum.ewm.dto.ewm_service.event.update_event.UpdateEventUserRequest;
import ru.practicum.ewm.dto.ewm_service.event.params.FullEventRequestParams;
import ru.practicum.ewm.dto.ewm_service.event.params.ShortEventRequestParams;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface EventService {

    EventFullDto addEvent(Long userId, NewEventDto newEventDto);

    List<EventFullDto> getEventsByParams(FullEventRequestParams fullEventRequestParams);

    EventFullDto updateEventAdminRequestByEventId(Long eventId, UpdateEventAdminRequest request);

    List<EventShortDto> getEventsBySearchWithParams(ShortEventRequestParams shortEventRequestParams, HttpServletRequest request);

    EventFullDto getEventById(Long eventId, HttpServletRequest request);

    List<EventShortDto> getEventByUserId(Long userId, int from, int size);

    EventFullDto getEventByUserIdAndEventId(Long userId, Long eventId);

    EventFullDto updateEventByUserIdAndEventId(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    List<ParticipationRequestDto> getEventParticipationListByEventId(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateEventRequestStatusByEventId(Long userId, Long eventId, EventRequestStatusUpdateRequest request);
}
