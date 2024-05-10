package ru.practicum.ewm.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.ewm_service.participation.ParticipationRequestDto;
import ru.practicum.ewm.dto.ewm_service.participation.enums.RequestParticipateStatus;
import ru.practicum.ewm.dto.ewm_service.user.NewUserRequest;
import ru.practicum.ewm.dto.ewm_service.user.UserDto;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.service.helper.EventValidationHelper;
import ru.practicum.ewm.mark.service.MarkService;
import ru.practicum.ewm.participation.mapper.ParticipationMapper;
import ru.practicum.ewm.participation.service.helper.ParticipationValidationHelper;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.participation.model.Participation;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.participation.repository.ParticipationRepository;
import ru.practicum.ewm.user.repository.UserRepository;
import ru.practicum.ewm.user.service.helper.UserValidationHelper;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final MarkService markService;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ParticipationRepository participationRepository;

    private final UserMapper userMapper;
    private final ParticipationMapper participationMapper;

    private final UserValidationHelper userValidationHelper;
    private final EventValidationHelper eventValidationHelper;
    private final ParticipationValidationHelper participationValidationHelper;

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
    @Transactional
    public void deleteUser(Long userId) {
        userValidationHelper.isUserPresent(userId);
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
        markService.addRatingsToUsers(users);
        List<UserDto> userDtoList = userMapper.userListToUserDtoList(users);
        log.info("Список пользователей с номера {} размером {} возвращён.", from, userDtoList.size());
        return userDtoList;
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByUserId(Long userId) {
        userValidationHelper.isUserPresent(userId);
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
        Event event = eventValidationHelper.isEventPresent(eventId);
        User user = userValidationHelper.isUserPresent(userId);
        Double rating = userRepository.getRatingByUserId(userId);

        if (rating != null) {
            user.setRating(rating);
        }

        participationValidationHelper.isParticipationValid(userId, event);
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
        userValidationHelper.isUserPresent(userId);
        Participation participation = participationValidationHelper.isParticipationPresent(requestId);
        userValidationHelper.isUserRequester(userId, participation.getRequester().getId());
        participation.setStatus(RequestParticipateStatus.CANCELED);
        participationRepository.save(participation);
        ParticipationRequestDto participationRequestDto = participationMapper.toParticipationRequestDto(participation);
        log.info("Заявка с ИД {} на участие в событии отменена пользователем с ИД {}.", requestId, userId);
        return participationRequestDto;
    }
}
