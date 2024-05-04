package ru.practicum.ewm.compilation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.dto.ewm_service.compilation.CompilationDto;
import ru.practicum.ewm.dto.ewm_service.compilation.NewCompilationDto;
import ru.practicum.ewm.event.model.Event;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface CompilationMapper {

    @Mapping(source = "eventList", target = "events")
    Compilation newCompilationDtoToCompilation(NewCompilationDto newCompilationDto, Set<Event> eventList);

    @Mapping(source = "compilation.events", target = "events")
    CompilationDto compilationToCompilationDto(Compilation compilation);

    List<CompilationDto> compilationListToCompilationDtoList(List<Compilation> compilationList);
}
