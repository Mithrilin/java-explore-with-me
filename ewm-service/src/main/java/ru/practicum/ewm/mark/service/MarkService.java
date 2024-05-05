package ru.practicum.ewm.mark.service;

import ru.practicum.ewm.dto.ewm_service.event.EventShortDto;
import ru.practicum.ewm.dto.ewm_service.user.UserDto;
import ru.practicum.ewm.user.model.User;

import java.util.List;

public interface MarkService {

    void addMark(long userId, long eventId, int markValue);

    void deleteMark(long userId, long eventId);

    void updateMark(long userId, long eventId, int markValue);

    List<EventShortDto> getEventRating(int from, int size);

    List<UserDto> getUserRating(int from, int size);

    void addRatingsToUsers(List<User> users);

    List<EventShortDto> getRecommendations(long userId, int from, int size);
}
