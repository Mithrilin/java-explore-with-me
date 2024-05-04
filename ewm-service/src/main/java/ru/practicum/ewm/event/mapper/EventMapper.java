package ru.practicum.ewm.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.dto.ewm_service.event.EventFullDto;
import ru.practicum.ewm.dto.ewm_service.event.EventShortDto;
import ru.practicum.ewm.dto.ewm_service.event.NewEventDto;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.user.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "id", constant = "0L")
    @Mapping(source = "category", target = "category")
    @Mapping(source = "user", target = "initiator")
    @Mapping(source = "location", target = "location")
    Event newEventDtoToEvent(NewEventDto newEventDto,
                             User user,
                             Category category,
                             Location location);

    @Mapping(source = "event.category", target = "category")
    @Mapping(source = "event.initiator", target = "initiator")
    @Mapping(source = "event.location", target = "location")
    EventFullDto eventToEventFullDto(Event event);

    @Mapping(source = "event.initiator", target = "initiator")
    EventShortDto eventToEventShortDto(Event event);

    List<EventShortDto> eventListToEventShortDtoList(List<Event> eventList);

    List<EventFullDto> eventListToEventFullDtoList(List<Event> eventList);
}
