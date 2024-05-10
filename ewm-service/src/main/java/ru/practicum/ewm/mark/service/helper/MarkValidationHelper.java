package ru.practicum.ewm.mark.service.helper;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.ewm_service.event.enums.EventLifecycleStates;
import ru.practicum.ewm.dto.ewm_service.participation.enums.RequestParticipateStatus;
import ru.practicum.ewm.dto.exception.ConflictException;
import ru.practicum.ewm.dto.exception.NotFoundException;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.mark.model.Mark;
import ru.practicum.ewm.mark.repository.MarkRepository;
import ru.practicum.ewm.participation.model.Participation;
import ru.practicum.ewm.participation.repository.ParticipationRepository;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class MarkValidationHelper {
    private final MarkRepository markRepository;
    private final ParticipationRepository participationRepository;

    public Mark isMarkPresent(Long userId, Long eventId) {
        Optional<Mark> optionalMark = markRepository.findByUser_IdAndEvent_Id(userId, eventId);

        if (optionalMark.isEmpty()) {
            log.error("Пользователь с ИД {} не оценивал событие с ИД {}.", userId, eventId);
            throw new NotFoundException(String.format("Пользователь с ИД %d не оценивал событие с ИД %d.",
                    userId, eventId));
        }

        return optionalMark.get();
    }

    public void isUserNotInitiator(Long userId, Event event) {
        Long eventId = event.getId();
        if (userId.equals(event.getInitiator().getId())) {
            log.error("Пользователь с ИД {} не может оценивать своё собственное событие с ИД {}.", userId, eventId);
            throw new ConflictException(String.format("Пользователь с ИД %d не может оценивать своё собственное " +
                    "событие с ИД %d.", userId, eventId));
        }
    }

    public void isEventDateInPast(Event event) {
        Long eventId = event.getId();
        if (event.getEventDate().isAfter(LocalDateTime.now())) {
            log.error("Дата начала оцениваемого события с ИД {} должна быть в прошлом.", eventId);
            throw new ConflictException(String.format("Дата начала оцениваемого события с ИД %d должна быть в прошлом.",
                    eventId));
        }
    }

    public void isEventPublished(Event event) {
        Long eventId = event.getId();
        if (event.getState() != EventLifecycleStates.PUBLISHED) {
            log.error("Оцениваемое событие с ИД {} должно быть в статусе PUBLISHED.", eventId);
            throw new ConflictException(String.format("Оцениваемое событие с ИД %d должно быть в статусе PUBLISHED.",
                    eventId));
        }
    }

    public void isUserParticipant(Long userId, Long eventId, Optional<Participation> optionalParticipation) {
        if (optionalParticipation.isEmpty()
                || optionalParticipation.get().getStatus() != RequestParticipateStatus.CONFIRMED) {
            log.error("Пользователь с ИД {} не являлся участником события с ИД {}.", userId, eventId);
            throw new ConflictException(String.format("Пользователь с ИД %d не являлся участником события с ИД %d.",
                    userId, eventId));
        }
    }

    public void isUserNotEvaluator(Long userId, Long eventId, Optional<Mark> optionalMark) {
        if (optionalMark.isPresent()) {
            log.error("Пользователь с ИД {} уже оценивал событие с ИД {}.", userId, eventId);
            throw new ConflictException(String.format("Пользователь с ИД %d уже оценивал событие с ИД %d.",
                    userId, eventId));
        }
    }

    public void checkMarkBeforeAdd(User user, Event event) {
        Long userId = user.getId();
        Long eventId = event.getId();
        isUserNotInitiator(userId, event);
        isEventDateInPast(event);
        isEventPublished(event);
        Optional<Participation> optionalParticipation =
                participationRepository.findByEvent_IdAndRequester_Id(event.getId(), user.getId());
        isUserParticipant(userId, eventId, optionalParticipation);
        Optional<Mark> optionalMark = markRepository.findByUser_IdAndEvent_Id(userId, eventId);
        isUserNotEvaluator(userId, eventId, optionalMark);
    }
}
