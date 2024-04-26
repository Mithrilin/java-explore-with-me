package ru.practicum.ewm.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.ewm_service.event.EventFullDto;
import ru.practicum.ewm.dto.ewm_service.event.ParticipationRequestDto;
import ru.practicum.ewm.dto.ewm_service.user.NewUserRequest;
import ru.practicum.ewm.dto.ewm_service.user.UserDto;
import ru.practicum.ewm.dto.exception.ConflictException;
import ru.practicum.ewm.dto.exception.NotFoundException;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto addUser(NewUserRequest newUserRequest) {
        User user = userMapper.toUser(newUserRequest);
        User returnedUser = userRepository.save(user);
        UserDto returnedUserDto = userMapper.toUserDto(returnedUser);
        log.info("Добавлен новый пользователь с ID = {}", returnedUserDto.getId());
        return returnedUserDto;
    }

    @Override
    public void deleteUser(Long userId) {
        isUserPresent(userId);
        userRepository.deleteById(userId);
        log.info("Пользователь с ID {} удалён.", userId);
    }

    @Override
    public List<UserDto> getUsersByIds(List<Long> ids, int from, int size) {
        int page = from / size;
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<User> usersPage;

        if (ids == null) {
            usersPage = userRepository.findAll(pageRequest);
        } else {
            usersPage = userRepository.findByIdIn(ids, pageRequest);
        }

        if (usersPage.isEmpty()) {
            log.info("Не нашлось пользователей по заданным параметрам.");
            return new ArrayList<>();
        }

        List<User> users = usersPage.getContent();
        List<UserDto> userDtoList= userMapper.toUserDtoList(users);
        log.info("Список пользователей с номера {} размером {} возвращён.", from, userDtoList.size());
        return userDtoList;
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByUserId(Long userId) {
        return null;
    }

    @Override
    public ParticipationRequestDto addParticipation(Long userId, Long eventId) {
        Event event = isEventPresent(eventId);


        return null;
    }

    @Override
    public EventFullDto cancelParticipationByUserId(Long userId, Long requestId) {
        return null;
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
        if (event.getInitiator().getId().equals(userId)) {
            log.error("Пользователь с ИД {} является создателем события с ИД {}.", userId, event.getId());
            throw new ConflictException(String.format("Пользователь с ИД %d является создателем события с ИД %d",
                    userId, event.getId()));
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
}
