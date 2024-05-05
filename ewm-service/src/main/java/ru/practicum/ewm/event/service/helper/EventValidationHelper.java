package ru.practicum.ewm.event.service.helper;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.ewm_service.event.enums.EventLifecycleStates;
import ru.practicum.ewm.dto.ewm_service.event.update_event.UpdateEventAdminRequest;
import ru.practicum.ewm.dto.exception.BadRequestException;
import ru.practicum.ewm.dto.exception.ConflictException;
import ru.practicum.ewm.dto.exception.NotFoundException;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.participation.service.helper.ParticipationValidationHelper;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class EventValidationHelper {
    private final EventRepository eventRepository;
    private final ParticipationValidationHelper participationValidationHelper;

    public Event isEventPresent(Long eventId) {
        Optional<Event> optionalEvent = eventRepository.findById(eventId);

        if (optionalEvent.isEmpty()) {
            log.error("Событие с ИД {} отсутствует в БД.", eventId);
            throw new NotFoundException(String.format("Событие с ИД %d отсутствует в БД.", eventId));
        }

        return optionalEvent.get();
    }

    public void isEventDateInFuture(UpdateEventAdminRequest request) {
        if (request.getEventDate() != null && request.getEventDate().isBefore(LocalDateTime.now())) {
            log.error("Дата начала изменяемого события должна быть в будущем.");
            throw new BadRequestException("Дата начала изменяемого события должна быть в будущем.");
        }
    }

    public void isEventStatePending(Event event) {
        if (event.getState() != EventLifecycleStates.PENDING) {
            log.error("Событие с ИД {} не в статусе ожидания.", event.getId());
            throw new ConflictException(String.format("Событие с ИД %d не в статусе ожидания.", event.getId()));
        }
    }

    public void isEventDateValid(LocalDateTime publishedOn, Event event) {
        long minDiffBeforeEventDate = 1;
        if (publishedOn.plusHours(minDiffBeforeEventDate).isAfter(event.getEventDate())) {
            log.error("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации");
            throw new ConflictException("Дата начала изменяемого события должна быть не ранее чем за час " +
                    "от даты публикации");
        }
    }

    public void isEventNotPublished(Event event) {
        if (event.getState() == EventLifecycleStates.PUBLISHED) {
            log.error("Событие с ИД {} уже опубликовано.", event.getId());
            throw new ConflictException(String.format("Событие с ИД %d уже опубликовано.", event.getId()));
        }
    }

    public void isEventPublished(Event event) {
        if (event.getState() != EventLifecycleStates.PUBLISHED) {
            log.error("Событие с ИД {} не опубликовано.", event.getId());
            throw new NotFoundException(String.format("Событие с ИД %d не опубликовано.", event.getId()));
        }
    }

    public void isConfirmationNotRequired(Event event) {
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            log.error("Для события с ИД {} подтверждение заявок не требуется.", event.getId());
            throw new BadRequestException(String.format("Для события с ИД %d подтверждение заявок не требуется.",
                    event.getId()));
        }
    }

    public void checkEventBeforeUpdateStatus(Event event) {
        isConfirmationNotRequired(event);
        participationValidationHelper.isParticipantLimitNotReached(event);
    }
}
