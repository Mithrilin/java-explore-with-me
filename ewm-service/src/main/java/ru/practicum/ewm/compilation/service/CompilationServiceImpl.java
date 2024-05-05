package ru.practicum.ewm.compilation.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.compilation.service.helper.CompilationValidationHelper;
import ru.practicum.ewm.dto.ewm_service.compilation.CompilationDto;
import ru.practicum.ewm.dto.ewm_service.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.ewm_service.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@AllArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    private final CompilationMapper compilationMapper;

    private final CompilationValidationHelper compilationValidationHelper;

    @Override
    @Transactional
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        Set<Event> eventList = new HashSet<>();

        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            eventList = eventRepository.findByIdIn(newCompilationDto.getEvents());
        }

        Compilation compilation = compilationMapper.newCompilationDtoToCompilation(newCompilationDto, eventList);
        Compilation returnedCompilation = compilationRepository.save(compilation);
        CompilationDto compilationDto = compilationMapper.compilationToCompilationDto(returnedCompilation);
        log.info("Добавлена новая подборка событий с ID = {}", compilationDto.getId());
        return compilationDto;
    }

    @Override
    public List<CompilationDto> getAllCompilations(Boolean pinned, int from, int size) {
        int page = from / size;
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<Compilation> compilationPage;

        if (pinned == null) {
            compilationPage = compilationRepository.findAll(pageRequest);
        } else {
            compilationPage = compilationRepository.findByPinned(pinned, pageRequest);
        }
        if (compilationPage.isEmpty()) {
            log.info("Не нашлось подборки событий по заданным параметрам.");
            return new ArrayList<>();
        }

        List<Compilation> compilationList = compilationPage.getContent();
        List<CompilationDto> compilationDtoList = compilationMapper.compilationListToCompilationDtoList(compilationList);
        log.info("Список подборок событий с номера {} размером {} возвращён.", from, compilationDtoList.size());
        return compilationDtoList;
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationValidationHelper.isCompilationPresent(compId);
        CompilationDto compilationDto = compilationMapper.compilationToCompilationDto(compilation);
        log.info("Подборка событий с ID = {} возвращена", compilationDto.getId());
        return compilationDto;
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        compilationValidationHelper.isCompilationPresent(compId);
        compilationRepository.deleteById(compId);
        log.info("Подборка событий с ID {} удалена.", compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilationById(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = compilationValidationHelper.isCompilationPresent(compId);
        updateCompilation(updateCompilationRequest, compilation);
        Compilation returnedCompilation = compilationRepository.save(compilation);
        CompilationDto compilationDto = compilationMapper.compilationToCompilationDto(returnedCompilation);
        log.info("Подборка событий с ID {} изменена.", compilationDto.getId());
        return compilationDto;
    }

    private void updateCompilation(UpdateCompilationRequest updateCompilationRequest,
                                   Compilation compilation) {
        if (updateCompilationRequest.getEvents() != null && !updateCompilationRequest.getEvents().isEmpty()) {
            Set<Event> eventList = eventRepository.findByIdIn(updateCompilationRequest.getEvents());
            compilation.setEvents(eventList);
        }
        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }
        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }
    }
}
