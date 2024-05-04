package ru.practicum.ewm.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.ewm_service.participation.ParticipationRequestDto;
import ru.practicum.ewm.dto.ewm_service.event.enums.EventLifecycleStates;
import ru.practicum.ewm.dto.ewm_service.participation.enums.RequestParticipateStatus;
import ru.practicum.ewm.dto.ewm_service.user.NewUserRequest;
import ru.practicum.ewm.dto.ewm_service.user.UserDto;
import ru.practicum.ewm.dto.exception.BadRequestException;
import ru.practicum.ewm.dto.exception.ConflictException;
import ru.practicum.ewm.dto.exception.NotFoundException;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.participation.mapper.ParticipationMapper;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.participation.model.Participation;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.participation.repository.ParticipationRepository;
import ru.practicum.ewm.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ParticipationRepository participationRepository;

    private final UserMapper userMapper;
    private final ParticipationMapper participationMapper;

    @Override
    @Transactional
    public UserDto addUser(NewUserRequest newUserRequest) {
        User user = userMapper.newUserRequestToUser(newUserRequest);
        User returnedUser = userRepository.save(user);
        UserDto returnedUserDto = userMapper.userToUserDto(returnedUser);
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
        List<UserDto> userDtoList = userMapper.userListToUserDtoList(users);
        log.info("Список пользователей с номера {} размером {} возвращён.", from, userDtoList.size());
        return userDtoList;
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByUserId(Long userId) {
        isUserPresent(userId);
        List<Participation> participationList = participationRepository.findByRequester_Id(userId);

        if (participationList.isEmpty()) {
            log.info("Для пользователя с ИД {} не нашлось ни одной заявки на участие в событиях.", userId);
            return new ArrayList<>();
        }

        List<ParticipationRequestDto> participationRequestDtoList =
                participationMapper.participationListToParticipationRequestDtoList(participationList);
        log.info("Список запросов на участие в событиях от пользователя с ИД {} размером {} возвращён.", userId,
                participationRequestDtoList.size());
        return participationRequestDtoList;
    }

    @Override
    @Transactional
    public ParticipationRequestDto addParticipation(Long userId, Long eventId) {
        Event event = isEventPresent(eventId);
        User user = isUserPresent(userId);
        isParticipationValid(userId, event);
        Participation participation = new Participation(
                null,
                LocalDateTime.now(),
                event,
                user,
                RequestParticipateStatus.PENDING
        );

        if (!event.getRequestModeration() || event.getParticipantLimit().equals(0)) {
            participation.setStatus(RequestParticipateStatus.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }

        Participation returnedParticipation = participationRepository.save(participation);
        ParticipationRequestDto participationRequestDto =
                participationMapper.toParticipationRequestDto(returnedParticipation);
        log.info("Добавлена заявка с ИД {} на участие в событии с ИД {}.", participationRequestDto.getId(),
                event.getId());
        return participationRequestDto;
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelParticipationByUserId(Long userId, Long requestId) {
        isUserPresent(userId);
        Participation participation = isParticipationPresent(requestId);
        isUserRequester(userId, participation.getRequester().getId());
        participation.setStatus(RequestParticipateStatus.CANCELED);
        participationRepository.save(participation);
        ParticipationRequestDto participationRequestDto = participationMapper.toParticipationRequestDto(participation);
        log.info("Заявка с ИД {} на участие в событии отменена пользователем с ИД {}.", requestId, userId);
        return participationRequestDto;
    }

    private User isUserPresent(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()) {
            log.error("Пользователь с ИД {} отсутствует в БД.", userId);
            throw new NotFoundException(String.format("Пользователь с ИД %d отсутствует в БД.", userId));
        }

        return optionalUser.get();
    }

    private void isUserRequester(Long userId, Long requesterId) {
        if (!userId.equals(requesterId)) {
            log.error("Пользователь с ИД {} не является создателем запроса на участие.", userId);
            throw new BadRequestException(String.format("Пользователь с ИД %d не является создателем запроса на участие.",
                    userId));
        }
    }

    private Participation isParticipationPresent(Long participationId) {
        Optional<Participation> optionalParticipation = participationRepository.findById(participationId);

        if (optionalParticipation.isEmpty()) {
            log.error("Запрос с ИД {} на участие в событии отсутствует в БД.", participationId);
            throw new NotFoundException(String.format("Запрос с ИД %d на участие в событии отсутствует в БД.",
                    participationId));
        }

        return optionalParticipation.get();
    }

    private void isParticipationValid(Long userId, Event event) {
        Long participantLimit = Long.valueOf(event.getParticipantLimit());
        Long confirmedRequests = event.getConfirmedRequests();
        Optional<Participation> optionalParticipationRequest =
                participationRepository.findByEvent_IdAndRequester_Id(event.getId(), userId);

        if (event.getInitiator().getId().equals(userId)
                || (participantLimit != 0 && participantLimit.equals(confirmedRequests))
                || event.getState() != EventLifecycleStates.PUBLISHED
                || optionalParticipationRequest.isPresent()) {
            log.error("Запрос от пользователя с ИД {} на участие в событии с ИД {} не может быть осуществлён.",
                    userId, event.getId());
            throw new ConflictException(String.format("Запрос от пользователя с ИД %d на участие в событии с ИД %d " +
                    "не может быть осуществлён.", userId, event.getId()));
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
