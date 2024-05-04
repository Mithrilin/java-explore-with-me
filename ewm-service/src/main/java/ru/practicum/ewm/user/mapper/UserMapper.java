package ru.practicum.ewm.user.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.dto.ewm_service.user.NewUserRequest;
import ru.practicum.ewm.dto.ewm_service.user.UserDto;
import ru.practicum.ewm.dto.ewm_service.user.UserShortDto;
import ru.practicum.ewm.user.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User newUserRequestToUser(NewUserRequest newUserRequest);

    UserDto userToUserDto(User user);

    List<UserDto> userListToUserDtoList(List<User> userList);

    UserShortDto userToUserShortDto(User user);
}
