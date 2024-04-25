package ru.practicum.ewm.user.service;

import ru.practicum.ewm.dto.ewm_service.event.EventFullDto;
import ru.practicum.ewm.dto.ewm_service.event.ParticipationRequestDto;
import ru.practicum.ewm.dto.ewm_service.user.NewUserRequest;
import ru.practicum.ewm.dto.ewm_service.user.UserDto;

import java.util.List;

public interface UserService {

    UserDto addUser(NewUserRequest newUserRequest);

    void deleteUser(long userId);

    List<UserDto> getUsersByIds(List<Long> ids, int from, int size);

    List<ParticipationRequestDto> getRequestsByUserId(long userId);

    ParticipationRequestDto addParticipation(long userId, long eventId);

    EventFullDto cancelParticipationByUserId(long userId, long requestId);
}
