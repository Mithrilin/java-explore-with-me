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
import ru.practicum.ewm.dto.exception.NotFoundException;
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
    public void deleteUser(long userId) {
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
    public List<ParticipationRequestDto> getRequestsByUserId(long userId) {
        return null;
    }

    @Override
    public ParticipationRequestDto addParticipation(long userId, long eventId) {
        return null;
    }

    @Override
    public EventFullDto cancelParticipationByUserId(long userId, long requestId) {
        return null;
    }

    private User isUserPresent(long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            log.error("Пользователь с ИД {} отсутствует в БД.", userId);
            throw new NotFoundException(String.format("Пользователь с ИД %d отсутствует в БД.", userId));
        }
        return optionalUser.get();
    }
}
