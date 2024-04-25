package ru.practicum.ewm.compilation.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.ewm_service.compilation.CompilationDto;
import ru.practicum.ewm.dto.ewm_service.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.ewm_service.compilation.UpdateCompilationRequest;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    @Override
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        return null;
    }

    @Override
    public List<CompilationDto> getAllCompilations(Boolean pinned, int from, int size) {
        return null;
    }

    @Override
    public CompilationDto getCompilationById(long compId) {
        return null;
    }

    @Override
    public void deleteCompilation(long compId) {

    }

    @Override
    public UpdateCompilationRequest updateCompilationById(long compId) {
        return null;
    }
}
