package ru.practicum.ewm.participation.service.helper;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.ewm_service.event.enums.EventLifecycleStates;
import ru.practicum.ewm.dto.ewm_service.participation.enums.RequestParticipateStatus;
import ru.practicum.ewm.dto.exception.ConflictException;
import ru.practicum.ewm.dto.exception.NotFoundException;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.participation.model.Participation;
import ru.practicum.ewm.participation.repository.ParticipationRepository;

import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class ParticipationValidationHelper {
    private final ParticipationRepository participationRepository;

    public Participation isParticipationPresent(Long participationId) {
        Optional<Participation> optionalParticipation = participationRepository.findById(participationId);

        if (optionalParticipation.isEmpty()) {
            log.error("Запрос с ИД {} на участие в событии отсутствует в БД.", participationId);
            throw new NotFoundException(String.format("Запрос с ИД %d на участие в событии отсутствует в БД.",
                    participationId));
        }

        return optionalParticipation.get();
    }

    public void isParticipantLimitNotReached(Event event) {
        if (event.getConfirmedRequests().equals(event.getParticipantLimit().longValue())) {
            log.error("В событии с ИД {} уже достигнут лимит участников.", event.getId());
            throw new ConflictException(String.format("В событии с ИД %d уже достигнут лимит участников.",
                    event.getId()));
        }
    }

    public void isParticipateStatusPending(Participation participation) {
        if (participation.getStatus() != RequestParticipateStatus.PENDING) {
            log.error("Статус у заявки на участие с ИД {} не PENDING.", participation.getId());
            throw new ConflictException(String.format("Статус у заявки на участие с ИД %d не PENDING.",
                    participation.getId()));
        }
    }

    public void isParticipationValid(Long userId, Event event) {
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
}
