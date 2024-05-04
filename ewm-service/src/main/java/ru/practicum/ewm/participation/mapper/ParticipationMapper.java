package ru.practicum.ewm.participation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.dto.ewm_service.participation.ParticipationRequestDto;
import ru.practicum.ewm.participation.model.Participation;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ParticipationMapper {

    @Mapping(source = "participation.event.id", target = "event")
    @Mapping(source = "participation.requester.id", target = "requester")
    ParticipationRequestDto toParticipationRequestDto(Participation participation);

    List<ParticipationRequestDto> participationListToParticipationRequestDtoList(List<Participation> participationList);
}
