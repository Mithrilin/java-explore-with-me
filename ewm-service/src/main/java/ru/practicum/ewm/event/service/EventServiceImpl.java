package ru.practicum.ewm.event.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.dto.ewm_service.event.EventFullDto;
import ru.practicum.ewm.dto.ewm_service.event.NewEventDto;
import ru.practicum.ewm.dto.exception.NotFoundException;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.mapper.LocationMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.repository.LocationRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class EventServiceImpl implements EventService {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;

    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;

    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        User user = isUserPresent(userId);
        Category category = isCategoryPresent(newEventDto.getCategory());
        Location location = locationMapper.toLocation(newEventDto.getLocation());
        Location returnedLocation = locationRepository.save(location);
        Event event = eventMapper.toEvent(newEventDto, user, category, returnedLocation);
        Event returnedEvent = eventRepository.save(event);
        EventFullDto eventFullDto = eventMapper.toEventFullDto(returnedEvent);
        log.info("Добавлено новое событие с ID = {}", returnedEvent.getId());
        return eventFullDto;
    }

    private User isUserPresent(long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            log.error("Пользователь с ИД {} отсутствует в БД.", userId);
            throw new NotFoundException(String.format("Пользователь с ИД %d отсутствует в БД.", userId));
        }
        return optionalUser.get();
    }

    private Category isCategoryPresent(long catId) {
        Optional<Category> optionalCategory = categoryRepository.findById(catId);
        if (optionalCategory.isEmpty()) {
            log.error("Категория с ИД {} отсутствует в БД.", catId);
            throw new NotFoundException(String.format("Категория с ИД %d отсутствует в БД.", catId));
        }
        return optionalCategory.get();
    }
}
