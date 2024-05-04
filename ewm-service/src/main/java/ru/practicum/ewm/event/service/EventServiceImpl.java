package ru.practicum.ewm.event.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
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
import ru.practicum.ewm.dto.exception.BadRequestException;
import ru.practicum.ewm.dto.exception.ConflictException;
import ru.practicum.ewm.dto.exception.NotFoundException;
import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.mapper.LocationMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.repository.LocationRepository;
import ru.practicum.ewm.participation.mapper.ParticipationMapper;
import ru.practicum.ewm.participation.model.Participation;
import ru.practicum.ewm.participation.repository.ParticipationRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class EventServiceImpl implements EventService {
    private static final LocalDateTime START = LocalDateTime.of(2000, 1, 1, 0, 0, 1);
    private static final String APP = "ewm-main-service";
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final ParticipationRepository participationRepository;

    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;
    private final ParticipationMapper participationMapper;

    private final StatsClient statsClient;

    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        User user = isUserPresent(userId);
        Category category = isCategoryPresent(newEventDto.getCategory());
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
        List<Specification<Event>> specifications = searchFullEventFilterToSpecifications(params);
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
        Event event = isEventPresent(eventId);
        isEventDateInFuture(request);
        updateEvent(event, request);

        if (request.getStateAction() != null) {
            if (request.getStateAction() == UpdateEventAdminStates.PUBLISH_EVENT) {
                isEventStatePending(event);
                LocalDateTime publishedOn = LocalDateTime.now();
                isEventDateValid(publishedOn, event);
                event.setPublishedOn(publishedOn);
                event.setState(EventLifecycleStates.PUBLISHED);
            } else {
                isEventNotPublished(event);
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
        List<Specification<Event>> specifications = searchShortEventFilterToSpecifications(params);
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
        Event event = isEventPresent(eventId);
        isEventPublished(event);
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
    public List<EventShortDto> getEventByUserId(Long userId, int from, int size) {
        isUserPresent(userId);
        int page = from / size;
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        List<Event> eventList = eventRepository.findByInitiator_Id(userId, pageRequest);
        List<EventShortDto> eventShortDtoList = eventMapper.eventListToEventShortDtoList(eventList);
        log.info("Список событий пользователя с ИД {} с номера {} размером {} возвращён.", userId, from,
                eventShortDtoList.size());
        return eventShortDtoList;
    }

    @Override
    public EventFullDto getEventByUserIdAndEventId(Long userId, Long eventId) {
        Event event = isEventPresent(eventId);
        isUserInitiator(userId, event);
        EventFullDto eventFullDto = eventMapper.eventToEventFullDto(event);
        log.info("Событие с ИД {} пользователя с ID {} возвращено.", eventId, userId);
        return eventFullDto;
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUserIdAndEventId(Long userId, Long eventId, UpdateEventUserRequest request) {
        isUserPresent(userId);
        Event event = isEventPresent(eventId);
        isEventNotPublished(event);
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
        isUserPresent(userId);
        Event event = isEventPresent(eventId);
        isUserInitiator(userId, event);
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
        isUserPresent(userId);
        Event event = isEventPresent(eventId);
        checkEventBeforeUpdateStatus(event);
        List<Long> requestIds = request.getRequestIds();
        List<Participation> participationList = participationRepository.findByIdIn(requestIds);
        List<Participation> confirmedRequests = new ArrayList<>();
        List<Participation> rejectedRequests = new ArrayList<>();

        if (request.getStatus() == EventRequestStatusUpdate.CONFIRMED) {
            for (Participation p : participationList) {
                isParticipateStatusPending(p);
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

    private void checkEventBeforeUpdateStatus(Event event) {
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            log.error("Для события с ИД {} подтверждение заявок не требуется.", event.getId());
            throw new BadRequestException(String.format("Для события с ИД %d подтверждение заявок не требуется.",
                    event.getId()));
        }

        if (event.getConfirmedRequests().equals(event.getParticipantLimit().longValue())) {
            log.error("В событии с ИД {} уже достигнут лимит участников.", event.getId());
            throw new ConflictException(String.format("В событии с ИД %d уже достигнут лимит участников.",
                    event.getId()));
        }
    }

    private User isUserPresent(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()) {
            log.error("Пользователь с ИД {} отсутствует в БД.", userId);
            throw new NotFoundException(String.format("Пользователь с ИД %d отсутствует в БД.", userId));
        }

        return optionalUser.get();
    }

    private void isUserInitiator(Long userId, Event event) {
        if (!event.getInitiator().getId().equals(userId)) {
            log.error("Пользователь с ИД {} не является создателем события с ИД {}.", userId, event.getId());
            throw new BadRequestException(String.format("Пользователь с ИД %d не является создателем события с ИД %d",
                    userId, event.getId()));
        }
    }

    private Category isCategoryPresent(Long catId) {
        Optional<Category> optionalCategory = categoryRepository.findById(catId);

        if (optionalCategory.isEmpty()) {
            log.error("Категория с ИД {} отсутствует в БД.", catId);
            throw new NotFoundException(String.format("Категория с ИД %d отсутствует в БД.", catId));
        }

        return optionalCategory.get();
    }

    private void isParticipateStatusPending(Participation participation) {
        if (participation.getStatus() != RequestParticipateStatus.PENDING) {
            log.error("Статус у заявки на участие с ИД {} не PENDING.", participation.getId());
            throw new ConflictException(String.format("Статус у заявки на участие с ИД %d не PENDING.",
                    participation.getId()));
        }
    }

    private Event isEventPresent(Long eventId) {
        Optional<Event> optionalEvent = eventRepository.findById(eventId);

        if (optionalEvent.isEmpty()) {
            log.error("Событие с ИД {} отсутствует в БД.", eventId);
            throw new NotFoundException(String.format("Событие с ИД %d отсутствует в БД.", eventId));
        }

        return optionalEvent.get();
    }

    private void isEventNotPublished(Event event) {
        if (event.getState() == EventLifecycleStates.PUBLISHED) {
            log.error("Событие с ИД {} уже опубликовано.", event.getId());
            throw new ConflictException(String.format("Событие с ИД %d уже опубликовано.", event.getId()));
        }
    }

    private void isEventPublished(Event event) {
        if (event.getState() != EventLifecycleStates.PUBLISHED) {
            log.error("Событие с ИД {} не опубликовано.", event.getId());
            throw new NotFoundException(String.format("Событие с ИД %d не опубликовано.", event.getId()));
        }
    }

    private void isEventDateValid(LocalDateTime publishedOn, Event event) {
        long minDiffBeforeEventDate = 1;
        if (publishedOn.plusHours(minDiffBeforeEventDate).isAfter(event.getEventDate())) {
            log.error("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации");
            throw new ConflictException("Дата начала изменяемого события должна быть не ранее чем за час " +
                    "от даты публикации");
        }
    }

    private void isEventStatePending(Event event) {
        if (event.getState() != EventLifecycleStates.PENDING) {
            log.error("Событие с ИД {} не в статусе ожидания.", event.getId());
            throw new ConflictException(String.format("Событие с ИД %d не в статусе ожидания.", event.getId()));
        }
    }

    private void isEventDateInFuture(UpdateEventAdminRequest request) {
        if (request.getEventDate() != null && request.getEventDate().isBefore(LocalDateTime.now())) {
            log.error("Дата начала изменяемого события должна быть в будущем.");
            throw new BadRequestException("Дата начала изменяемого события должна быть в будущем.");
        }
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
            Category category = isCategoryPresent(categoryId);
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

    private List<Specification<Event>> searchFullEventFilterToSpecifications(FullEventRequestParams params) {
        List<Specification<Event>> specifications = new ArrayList<>();
        specifications.add(params.getUsers() == null ? null : userIdIn(params.getUsers()));
        specifications.add(params.getStates() == null ? null : statesIn(params.getStates()));
        specifications.add(params.getCategories() == null ? null : categoryIdIn(params.getCategories()));
        specifications.add(params.getRangeStart() == null ? null : eventDateAfter(params.getRangeStart()));
        specifications.add(params.getRangeEnd() == null ? null : eventDateBefore(params.getRangeEnd()));
        return specifications.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<Specification<Event>> searchShortEventFilterToSpecifications(ShortEventRequestParams params) {
        List<Specification<Event>> specifications = new ArrayList<>();
        specifications.add(stateEqual());
        specifications.add(params.getText() == null ? null : annotationOrDescriptionLike(params.getText().toLowerCase()));
        specifications.add(params.getCategories() == null ? null : categoryIdIn(params.getCategories()));
        specifications.add(params.getPaid() == null ? null : paidEqual(params.getPaid()));

        if (params.getRangeStart() == null && params.getRangeEnd() == null) {
            specifications.add(eventDateAfterNow());
        } else {
            specifications.add(params.getRangeStart() == null ? null : eventDateAfter(params.getRangeStart()));
            specifications.add(params.getRangeEnd() == null ? null : eventDateBefore(params.getRangeEnd()));
        }

        specifications.add(params.getOnlyAvailable() == null ? null : confirmedRequestsLessThan(params.getOnlyAvailable()));
        return specifications.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private Specification<Event> annotationOrDescriptionLike(String values) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), values),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), values)
        );
    }

    private Specification<Event> stateEqual() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("state"), EventLifecycleStates.PUBLISHED);
    }

    private Specification<Event> paidEqual(Boolean values) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("paid"), values);
    }

    private Specification<Event> confirmedRequestsLessThan(Boolean onlyAvailable) {
        if (onlyAvailable) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get("confirmedRequests"),
                    root.get("participantLimit"));
        } else {
            return null;
        }
    }

    private Specification<Event> userIdIn(List<Long> values) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.in(root.get("initiator").get("id")).value(values);
    }

    private Specification<Event> statesIn(List<EventLifecycleStates> values) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.in(root.get("state")).value(values);
    }

    private Specification<Event> categoryIdIn(List<Long> values) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.in(root.get("category").get("id")).value(values);
    }

    private Specification<Event> eventDateAfter(LocalDateTime values) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), values);
    }

    private Specification<Event> eventDateAfterNow() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"),
                LocalDateTime.now());
    }

    private Specification<Event> eventDateBefore(LocalDateTime values) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), values);
    }
}
