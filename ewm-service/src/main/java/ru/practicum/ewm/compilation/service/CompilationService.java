package ru.practicum.ewm.compilation.service;

import ru.practicum.ewm.dto.ewm_service.compilation.CompilationDto;
import ru.practicum.ewm.dto.ewm_service.compilation.NewCompilationDto;

public interface CompilationService {

    CompilationDto addCompilation(NewCompilationDto newCompilationDto);
}
