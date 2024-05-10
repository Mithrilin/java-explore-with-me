package ru.practicum.ewm.mark.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.ewm_service.event.EventShortDto;
import ru.practicum.ewm.dto.ewm_service.event.enums.EventLifecycleStates;
import ru.practicum.ewm.dto.ewm_service.user.UserDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.service.helper.EventValidationHelper;
import ru.practicum.ewm.mark.model.Mark;
import ru.practicum.ewm.mark.params.RecommendationsParams;
import ru.practicum.ewm.mark.repository.MarkRepository;
import ru.practicum.ewm.mark.service.helper.MarkValidationHelper;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;
import ru.practicum.ewm.user.service.helper.UserValidationHelper;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class MarkServiceImpl implements MarkService {
    private static final int MIN_NUMBER_OF_RECOMMENDATIONS = 5;
    private static final double CORRECTION_COEFFICIENT = 10;
    private final MarkRepository markRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    private final EventMapper eventMapper;
    private final UserMapper userMapper;

    private final UserValidationHelper userValidationHelper;
    private final EventValidationHelper eventValidationHelper;
    private final MarkValidationHelper markValidationHelper;

    @Override
    @Transactional
    public void addMark(long userId, long eventId, int markValue) {
        User user = userValidationHelper.isUserPresent(userId);
        Event event = eventValidationHelper.isEventPresent(eventId);
        markValidationHelper.checkMarkBeforeAdd(user, event);
        Mark mark = new Mark();
        mark.setUser(user);
        mark.setEvent(event);
        mark.setMarkValue(markValue);
        markRepository.save(mark);
        addRatingToEvent(event);
        eventRepository.save(event);
        log.info("Пользователь с ИД {} оценил событие с ИД {}.", userId, eventId);
    }

    @Override
    @Transactional
    public void deleteMark(long userId, long eventId) {
        userValidationHelper.isUserPresent(userId);
        Event event = eventValidationHelper.isEventPresent(eventId);
        Mark mark = markValidationHelper.isMarkPresent(userId, eventId);
        markRepository.deleteById(mark.getId());
        addRatingToEvent(event);
        eventRepository.save(event);
        log.info("Оценка пользователя с ИД {} события с ИД {} удалена.", userId, eventId);
    }

    @Override
    @Transactional
    public void updateMark(long userId, long eventId, int markValue) {
        userValidationHelper.isUserPresent(userId);
        Event event = eventValidationHelper.isEventPresent(eventId);
        Mark mark = markValidationHelper.isMarkPresent(userId, eventId);

        if (mark.getMarkValue().equals(markValue)) {
            log.info("Новая оценка пользователя с ИД {} события с ИД {} идентична старой.", userId, eventId);
            return;
        }

        mark.setMarkValue(markValue);
        markRepository.save(mark);
        addRatingToEvent(event);
        eventRepository.save(event);
        log.info("Оценка пользователя с ИД {} события с ИД {} обновлена.", userId, eventId);
    }

    @Override
    public List<EventShortDto> getEventRating(int from, int size) {
        int page = from / size;
        Sort sort = Sort.by(Sort.Direction.DESC, "rating");
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<Event> eventPage = eventRepository.findByStateLike(EventLifecycleStates.PUBLISHED, pageRequest);

        if (eventPage.isEmpty()) {
            log.info("Не нашлось опубликованных событий.");
            return new ArrayList<>();
        }

        List<Event> eventList = eventPage.getContent();
        List<EventShortDto> eventShortDtoList = eventMapper.eventListToEventShortDtoList(eventList);
        log.info("Рейтинг событий с номера {} размером {} возвращён.", from, eventShortDtoList.size());
        return eventShortDtoList;
    }

    @Override
    public List<UserDto> getUserRating(int from, int size) {
        int page = from / size;
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAllWithRatingSort(pageRequest);

        if (userPage.isEmpty()) {
            log.info("Пользователей нет в БД.");
            return new ArrayList<>();
        }

        List<User> userList = userPage.getContent();
        addRatingsToUsers(userList);
        List<UserDto> userDtoList = userMapper.userListToUserDtoList(userList);
        log.info("Рейтинг пользователей с номера {} размером {} возвращён.", from, userDtoList.size());
        return userDtoList;
    }

    @Override
    public void addRatingsToUsers(List<User> users) {
        List<Long> userIdList = users.stream().map(User::getId).collect(Collectors.toList());
        List<Map<String, Object>> columnNameToColumnValueMapList = userRepository.findAllRatingsByUserIdIn(userIdList);
        Map<Long, Double> userIdToUserRating = columnNameToColumnValueMapList.stream()
                .collect(Collectors.toMap(e -> Long.valueOf(e.get("id").toString()),
                        e -> e.get("user_rating") == null ? 0
                                : Double.parseDouble(e.get("user_rating").toString()), (a, b) -> b));
        users.forEach(user -> user.setRating(userIdToUserRating.get(user.getId()) == null ? 0
                : userIdToUserRating.get(user.getId())));
    }

    @Override
    public List<EventShortDto> getRecommendations(long requesterId, int from, int size) {
        Map<Long, HashMap<Long, Integer>> userIdToEventIdWithDiff = new HashMap<>();
        Map<Long, HashMap<Long, Integer>> userIdToEventIdWithMarkValue = new HashMap<>();
        Map<Long, Integer> userIdToMatch = new HashMap<>();
        RecommendationsParams params = new RecommendationsParams(
                userIdToEventIdWithDiff,
                userIdToEventIdWithMarkValue,
                userIdToMatch,
                requesterId
        );
        addUserIdToEventIdWithMark(params);
        calculateDifferencesAndMatchesBetweenUsers(params);
        List<Event> eventsForRecommendations = getEventIdsForRecommendations(params);

        if (eventsForRecommendations.isEmpty()) {
            log.info("Список рекомендаций для пользователя с id {} пуст.", requesterId);
            return new ArrayList<>();
        }

        List<EventShortDto> eventShortDtoListForRecommendations =
                eventMapper.eventListToEventShortDtoList(eventsForRecommendations);
        log.info("Список рекомендаций для пользователя с id {} возвращён.", requesterId);
        return eventShortDtoListForRecommendations;
    }

    private List<Event> getEventIdsForRecommendations(RecommendationsParams params) {
        Long userIdWithMinDiff = getUserIdWithMinDiff(params);
        List<Event> eventsForRecommendations = eventRepository.findAllByRequesterIdAndUserId(params.getRequesterId(),
                userIdWithMinDiff, LocalDateTime.now());
        log.info("Список событий для рекомендации на основе пользователя с ИД {} размером {} получен из БД",
                userIdWithMinDiff, eventsForRecommendations.size());

        if (eventsForRecommendations.size() < MIN_NUMBER_OF_RECOMMENDATIONS) {
            log.info("Список событий для рекомендации на основе похожих оценок пользователя меньше минимального " +
                    "значения {}. Начат альтернативный способ формирования списка, на основе рейтинга пользователей.",
                    MIN_NUMBER_OF_RECOMMENDATIONS);
            eventsForRecommendations.addAll(eventRepository.findAllByUserRating(params.getRequesterId(),
                    LocalDateTime.now(), MIN_NUMBER_OF_RECOMMENDATIONS));
        }

        return eventsForRecommendations;
    }

    private Long getUserIdWithMinDiff(RecommendationsParams params) {
        log.info("Начат поиск пользователя с минимальной разницей в оценках.");
        Long userIdWithMinDiff = 0L;
        double minDiffCount = Double.MAX_VALUE;

        for (Map.Entry<Long, HashMap<Long, Integer>> checkedUserIdToEventIdWithDiff
                : params.getUserIdToEventIdWithDiff().entrySet()) {
            long checkedUserId = checkedUserIdToEventIdWithDiff.getKey();

            int sumDiff = checkedUserIdToEventIdWithDiff.getValue().values().stream().mapToInt(e -> e).sum();
            double diffCount = (sumDiff + CORRECTION_COEFFICIENT) / params.getUserIdToMatch().get(checkedUserId);
            log.info("diffCount для пользователя с ИД {} составляет {}.", checkedUserId, diffCount);

            if ((diffCount < minDiffCount)
                    || ((diffCount == minDiffCount)
                    && (params.getUserIdToMatch().get(checkedUserId)
                    > params.getUserIdToMatch().get(userIdWithMinDiff)))) {
                minDiffCount = diffCount;
                userIdWithMinDiff = checkedUserId;
                log.info("Значение ИД пользователя с минимальной разницей в оценках обновлено. ИД = {}",
                        userIdWithMinDiff);
            }
        }

        return userIdWithMinDiff;
    }

    private void calculateDifferencesAndMatchesBetweenUsers(RecommendationsParams params) {
        if (params.getUserIdToEventIdWithMarkValue().size() <= 1) {
            log.info("Хеш-таблица ИД пользователей с оценками на события пуста или в ней только целевой пользователь.");
            return;
        }

        for (Map.Entry<Long, HashMap<Long, Integer>> currentUserIdToEventIdWithMarkValue
                : params.getUserIdToEventIdWithMarkValue().entrySet()) {
            if (currentUserIdToEventIdWithMarkValue.getKey() != params.getRequesterId()) {
                enrichDifferencesAndMatches(currentUserIdToEventIdWithMarkValue, params);
            }
        }
        log.info("Информация о разницах в оценках между целевым и похожими пользователями успешно скалькулирована.");
    }

    private void enrichDifferencesAndMatches(Map.Entry<Long, HashMap<Long, Integer>> currentUserIdToEventIdWithMarkValue,
                                             RecommendationsParams params) {
        for (Map.Entry<Long, Integer> e : currentUserIdToEventIdWithMarkValue.getValue().entrySet()) {
            long userId = currentUserIdToEventIdWithMarkValue.getKey();

            if (!params.getUserIdToEventIdWithDiff().containsKey(userId)) {
                params.getUserIdToEventIdWithDiff().put(userId, new HashMap<>());
                params.getUserIdToMatch().put(userId, 0);
            }

            long eventId = e.getKey();
            int userMark = e.getValue();

            if (params.getUserIdToEventIdWithMarkValue().get(params.getRequesterId()).containsKey(eventId)) {
                int requesterMark = params.getUserIdToEventIdWithMarkValue().get(params.getRequesterId()).get(eventId);
                params.getUserIdToEventIdWithDiff().get(userId).put(eventId, Math.abs(requesterMark - userMark));
                int newMatchCount = params.getUserIdToMatch().get(userId) + 1;
                params.getUserIdToMatch().put(userId, newMatchCount);
            }
        }

        log.info("Информация о разницах в оценках между целевым пользователем и с ИД {} успешно добавлена в HashMap.",
                currentUserIdToEventIdWithMarkValue.getKey());
    }

    private void addUserIdToEventIdWithMark(RecommendationsParams params) {
        List<Mark> markListOfSimilarUsers = markRepository.findAllMarksOfSimilarUsers(params.getRequesterId());

        if (markListOfSimilarUsers.isEmpty()) {
            log.info("Список оценок похожих пользователей пуст.");
            return;
        }

        log.info("Список оценок похожих пользователей из БД размером {} получен.", markListOfSimilarUsers.size());
        Map<Long, HashMap<Long, Integer>> userIdToEventIdWithMarkValue = params.getUserIdToEventIdWithMarkValue();

        for (Mark mark : markListOfSimilarUsers) {
            long userId = mark.getUser().getId();
            long filmId = mark.getEvent().getId();
            int markValue = mark.getMarkValue();

            if (!userIdToEventIdWithMarkValue.containsKey(userId)) {
                userIdToEventIdWithMarkValue.put(userId, new HashMap<>());
            }

            userIdToEventIdWithMarkValue.get(userId).put(filmId, markValue);
        }
        log.info("Информация из оценок успешно сохранена в HashMap.");
    }

    private void addRatingToEvent(Event event) {
        Double rating = markRepository.getRatingByEventId(event.getId());
        if (rating != null) {
            event.setRating(rating);
        }
    }
}
