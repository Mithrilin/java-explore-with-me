package ru.practicum.ewm.event.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.service.helper.CategoryValidationHelper;
import ru.practicum.ewm.client.stats.StatsClient;
import ru.practicum.ewm.dto.ewm_service.event.EventFullDto;
import ru.practicum.ewm.dto.ewm_service.event.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.ewm_service.event.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.ewm_service.event.EventShortDto;
import ru.practicum.ewm.dto.ewm_service.event.NewEventDto;
import ru.practicum.ewm.dto.ewm_service.event.enums.EventRequestStatusUpdate;
import ru.practicum.ewm.dto.ewm_service.event.enums.UpdateEventAdminStates;
import ru.practicum.ewm.dto.ewm_service.event.update_event.UpdateEventRequest;
import ru.practicum.ewm.dto.ewm_service.participation.ParticipationRequestDto;
import ru.practicum.ewm.dto.ewm_service.event.update_event.UpdateEventAdminRequest;
import ru.practicum.ewm.dto.ewm_service.event.update_event.UpdateEventUserRequest;
import ru.practicum.ewm.dto.ewm_service.event.enums.EventLifecycleStates;
import ru.practicum.ewm.dto.ewm_service.event.enums.UpdateEventUserStates;
import ru.practicum.ewm.dto.ewm_service.event.params.FullEventRequestParams;
import ru.practicum.ewm.dto.ewm_service.event.params.ShortEventRequestParams;
import ru.practicum.ewm.dto.ewm_service.location.LocationDto;
import ru.practicum.ewm.dto.ewm_service.participation.enums.RequestParticipateStatus;
import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.mapper.LocationMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.repository.LocationRepository;
import ru.practicum.ewm.event.service.helper.EventValidationHelper;
import ru.practicum.ewm.event.service.specification.EventSpecifications;
import ru.practicum.ewm.participation.mapper.ParticipationMapper;
import ru.practicum.ewm.participation.model.Participation;
import ru.practicum.ewm.participation.repository.ParticipationRepository;
import ru.practicum.ewm.participation.service.helper.ParticipationValidationHelper;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;
import ru.practicum.ewm.user.service.helper.UserValidationHelper;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class EventServiceImpl implements EventService {
    private static final LocalDateTime START = LocalDateTime.of(2000, 1, 1, 0, 0, 1);
    private static final String APP = "ewm-main-service";

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final ParticipationRepository participationRepository;

    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;
    private final ParticipationMapper participationMapper;

    private final EventValidationHelper eventValidationHelper;
    private final UserValidationHelper userValidationHelper;
    private final CategoryValidationHelper categoryValidationHelper;
    private final ParticipationValidationHelper participationValidationHelper;

    private final StatsClient statsClient;

    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        User user = userValidationHelper.isUserPresent(userId);
        Double rating = userRepository.getRatingByUserId(userId);

        if (rating != null) {
            user.setRating(rating);
        }

        Category category = categoryValidationHelper.isCategoryPresent(newEventDto.getCategory());
        Location location = getLocation(newEventDto.getLocation());
        Event event = eventMapper.newEventDtoToEvent(newEventDto, user, category, location);
        Event returnedEvent = eventRepository.save(event);
        EventFullDto eventFullDto = eventMapper.eventToEventFullDto(returnedEvent);
        log.info("Добавлено новое событие с ID = {}", returnedEvent.getId());
        return eventFullDto;
    }

    @Override
    public List<EventFullDto> getEventsByParams(FullEventRequestParams params) {
        PageRequest pageRequest = params.getPageRequest();
        List<Specification<Event>> specifications = EventSpecifications.searchFullEventFilterToSpecifications(params);
        Page<Event> eventPage = eventRepository.findAll(specifications.stream().reduce(Specification::and)
                .orElse(null), pageRequest);

        if (eventPage.isEmpty()) {
            log.info("Не нашлось событий по заданным параметрам.");
            return new ArrayList<>();
        }

        List<Event> eventList = eventPage.getContent();
        List<EventFullDto> eventFullDtoList = eventMapper.eventListToEventFullDtoList(eventList);
        log.info("Список событий с номера {} размером {} возвращён.", params.getFrom(), eventFullDtoList.size());
        return eventFullDtoList;
    }

    @Override
    @Transactional
    public EventFullDto updateEventAdminRequestByEventId(Long eventId, UpdateEventAdminRequest request) {
        Event event = eventValidationHelper.isEventPresent(eventId);
        eventValidationHelper.isEventDateInFuture(request);
        updateEvent(event, request);

        if (request.getStateAction() != null) {
            if (request.getStateAction() == UpdateEventAdminStates.PUBLISH_EVENT) {
                eventValidationHelper.isEventStatePending(event);
                LocalDateTime publishedOn = LocalDateTime.now();
                eventValidationHelper.isEventDateValid(publishedOn, event);
                event.setPublishedOn(publishedOn);
                event.setState(EventLifecycleStates.PUBLISHED);
            } else {
                eventValidationHelper.isEventNotPublished(event);
                event.setState(EventLifecycleStates.CANCELED);
            }
        }

        eventRepository.save(event);
        EventFullDto eventFullDto = eventMapper.eventToEventFullDto(event);
        log.info("Событие с ИД {} обновлено.", eventId);
        return eventFullDto;
    }

    @Override
    public List<EventShortDto> getEventsBySearchWithParams(ShortEventRequestParams params, HttpServletRequest request) {
        PageRequest pageRequest = params.getPageRequest();
        List<Specification<Event>> specifications = EventSpecifications.searchShortEventFilterToSpecifications(params);
        Page<Event> eventPage = eventRepository.findAll(specifications.stream().reduce(Specification::and)
                .orElse(null), pageRequest);

        if (eventPage.isEmpty()) {
            log.info("Не нашлось событий по заданным параметрам.");
            return new ArrayList<>();
        }

        List<Event> eventList = eventPage.getContent();
        List<EventShortDto> eventShortDtoList = eventMapper.eventListToEventShortDtoList(eventList);
        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();
        EndpointHit endpointHit = new EndpointHit(APP, uri, ip, LocalDateTime.now());
        statsClient.addHitStats(endpointHit);
        log.info("Список событий с номера {} размером {} возвращён.", params.getFrom(), eventShortDtoList.size());
        return eventShortDtoList;
    }

    @Override
    public EventFullDto getEventById(Long eventId, HttpServletRequest request) {
        Event event = eventValidationHelper.isEventPresent(eventId);
        eventValidationHelper.isEventPublished(event);
        String uri = request.getRequestURI();
        List<String> uris = List.of(uri);
        Long views = getEventViews(uris);
        event.setViews(views);

        if (!event.getViews().equals(views)) {
            eventRepository.save(event);
        }

        EventFullDto eventFullDto = eventMapper.eventToEventFullDto(event);
        String ip = request.getRemoteAddr();
        EndpointHit endpointHit = new EndpointHit(APP, uri, ip, LocalDateTime.now());
        statsClient.addHitStats(endpointHit);
        log.info("Событие с ИД {} пользователя возвращено.", eventId);
        return eventFullDto;
    }

    @Override
    public List<EventShortDto> getEventsByUserId(Long userId, int from, int size) {
        userValidationHelper.isUserPresent(userId);
        int page = from / size;
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        List<Event> eventList = eventRepository.findByInitiator_Id(userId, pageRequest);

        if (eventList.isEmpty()) {
            log.info("Не нашлось событий, созданных пользователем с Ид {}.", userId);
            return new ArrayList<>();
        }

        List<EventShortDto> eventShortDtoList = eventMapper.eventListToEventShortDtoList(eventList);
        log.info("Список событий пользователя с ИД {} с номера {} размером {} возвращён.", userId, from,
                eventShortDtoList.size());
        return eventShortDtoList;
    }

    @Override
    public EventFullDto getEventByUserIdAndEventId(Long userId, Long eventId) {
        Event event = eventValidationHelper.isEventPresent(eventId);
        userValidationHelper.isUserInitiator(userId, event);
        EventFullDto eventFullDto = eventMapper.eventToEventFullDto(event);
        log.info("Событие с ИД {} пользователя с ID {} возвращено.", eventId, userId);
        return eventFullDto;
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUserIdAndEventId(Long userId, Long eventId, UpdateEventUserRequest request) {
        userValidationHelper.isUserPresent(userId);
        Event event = eventValidationHelper.isEventPresent(eventId);
        eventValidationHelper.isEventNotPublished(event);
        updateEvent(event, request);

        if (request.getStateAction() != null) {
            if (request.getStateAction() == UpdateEventUserStates.SEND_TO_REVIEW) {
                event.setState(EventLifecycleStates.PENDING);
            }
            if (request.getStateAction() == UpdateEventUserStates.CANCEL_REVIEW) {
                event.setState(EventLifecycleStates.CANCELED);
            }
        }

        eventRepository.save(event);
        EventFullDto eventFullDto = eventMapper.eventToEventFullDto(event);
        log.info("Событие с ИД {} пользователя с ID {} обновлено.", eventId, userId);
        return eventFullDto;
    }

    @Override
    public List<ParticipationRequestDto> getEventParticipationListByEventId(Long userId, Long eventId) {
        userValidationHelper.isUserPresent(userId);
        Event event = eventValidationHelper.isEventPresent(eventId);
        userValidationHelper.isUserInitiator(userId, event);
        List<Participation> participationList = participationRepository.findByEvent_Id(eventId);

        if (participationList.isEmpty()) {
            log.info("Для события с ИД {} не нашлось ни одной заявки на участие.", eventId);
            return new ArrayList<>();
        }

        List<ParticipationRequestDto> participationRequestDtoList =
                participationMapper.participationListToParticipationRequestDtoList(participationList);
        log.info("Список запросов на участие в событии с ИД {} размером {} возвращён.", eventId,
                participationRequestDtoList.size());
        return participationRequestDtoList;
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateEventRequestStatusByEventId(Long userId, Long eventId,
                                                                            EventRequestStatusUpdateRequest request) {
        userValidationHelper.isUserPresent(userId);
        Event event = eventValidationHelper.isEventPresent(eventId);
        eventValidationHelper.checkEventBeforeUpdateStatus(event);
        List<Long> requestIds = request.getRequestIds();
        List<Participation> participationList = participationRepository.findByIdIn(requestIds);
        List<Participation> confirmedRequests = new ArrayList<>();
        List<Participation> rejectedRequests = new ArrayList<>();

        if (request.getStatus() == EventRequestStatusUpdate.CONFIRMED) {
            for (Participation p : participationList) {
                participationValidationHelper.isParticipateStatusPending(p);
                if (event.getConfirmedRequests() < event.getParticipantLimit()) {
                    p.setStatus(RequestParticipateStatus.CONFIRMED);
                    confirmedRequests.add(p);
                    event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                } else {
                    p.setStatus(RequestParticipateStatus.REJECTED);
                    rejectedRequests.add(p);
                }
            }
        } else {
            participationList.forEach(p -> p.setStatus(RequestParticipateStatus.REJECTED));
            rejectedRequests.addAll(participationList);
        }

        List<ParticipationRequestDto> confirmedRequestsDto =
                participationMapper.participationListToParticipationRequestDtoList(confirmedRequests);
        List<ParticipationRequestDto> rejectedRequestsDto =
                participationMapper.participationListToParticipationRequestDtoList(rejectedRequests);
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult(
                confirmedRequestsDto,
                rejectedRequestsDto
        );
        log.info("Заявкам с ИД {} на участие в событии с ИД {} обновлён статус.", requestIds, event.getId());
        return result;
    }

    private Location getLocation(LocationDto locationDto) {
        Optional<Location> locationOptional = locationRepository.findByLatAndLon(locationDto.getLat(),
                locationDto.getLon());
        return locationOptional.orElseGet(() ->
                locationRepository.save(locationMapper.locationDtoToLocation(locationDto)));
    }

    private Long getEventViews(List<String> uris) {
        List<ViewStats> viewStatsList = statsClient.getViewStatsList(START, LocalDateTime.now(), uris, true);
        if (viewStatsList.isEmpty()) {
            return 0L;
        }
        ViewStats viewStats = viewStatsList.get(0);
        return viewStats.getHits();
    }

    private void updateEvent(Event event, UpdateEventRequest request) {
        if (request.getAnnotation() != null && !request.getAnnotation().isBlank()) {
            event.setAnnotation(request.getAnnotation());
        }
        Long categoryId = request.getCategory();
        if (categoryId != null && !categoryId.equals(event.getCategory().getId())) {
            Category category = categoryValidationHelper.isCategoryPresent(categoryId);
            event.setCategory(category);
        }
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            event.setDescription(request.getDescription());
        }
        if (request.getEventDate() != null) {
            request.setEventDate(request.getEventDate());
        }
        LocationDto locationDto = request.getLocation();
        if (locationDto != null
                && (!locationDto.getLat().equals(event.getLocation().getLat())
                && !locationDto.getLon().equals(event.getLocation().getLon()))) {
            Location location = getLocation(locationDto);
            event.setLocation(location);
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            event.setTitle(request.getTitle());
        }
    }
}
