package ru.practicum.ewm.user.service;

import ru.practicum.ewm.dto.ewm_service.event.EventFullDto;
import ru.practicum.ewm.dto.ewm_service.event.ParticipationRequestDto;
import ru.practicum.ewm.dto.ewm_service.user.NewUserRequest;
import ru.practicum.ewm.dto.ewm_service.user.UserDto;

import java.util.List;

public interface UserService {

    UserDto addUser(NewUserRequest newUserRequest);

    void deleteUser(Long userId);

    List<UserDto> getUsersByIds(List<Long> ids, int from, int size);

    List<ParticipationRequestDto> getRequestsByUserId(Long userId);

    ParticipationRequestDto addParticipation(Long userId, Long eventId);

    EventFullDto cancelParticipationByUserId(Long userId, Long requestId);
}
