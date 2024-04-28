package ru.practicum.ewm.compilation.service;

import ru.practicum.ewm.dto.ewm_service.compilation.CompilationDto;
import ru.practicum.ewm.dto.ewm_service.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.ewm_service.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    CompilationDto addCompilation(NewCompilationDto newCompilationDto);

    List<CompilationDto> getAllCompilations(Boolean pinned, int from, int size);

    CompilationDto getCompilationById(Long compId);

    void deleteCompilation(Long compId);

    CompilationDto updateCompilationById(Long compId, UpdateCompilationRequest updateCompilationRequest);
}
