package ru.practicum.ewm.user.service.helper;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.exception.BadRequestException;
import ru.practicum.ewm.dto.exception.NotFoundException;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class UserValidationHelper {
    private final UserRepository userRepository;

    public User isUserPresent(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()) {
            log.error("Пользователь с ИД {} отсутствует в БД.", userId);
            throw new NotFoundException(String.format("Пользователь с ИД %d отсутствует в БД.", userId));
        }

        return optionalUser.get();
    }

    public void isUserInitiator(Long userId, Event event) {
        if (!event.getInitiator().getId().equals(userId)) {
            log.error("Пользователь с ИД {} не является создателем события с ИД {}.", userId, event.getId());
            throw new BadRequestException(String.format("Пользователь с ИД %d не является создателем события с ИД %d",
                    userId, event.getId()));
        }
    }

    public void isUserRequester(Long userId, Long requesterId) {
        if (!userId.equals(requesterId)) {
            log.error("Пользователь с ИД {} не является создателем запроса на участие.", userId);
            throw new BadRequestException(String.format("Пользователь с ИД %d не является создателем запроса на участие.",
                    userId));
        }
    }
}
