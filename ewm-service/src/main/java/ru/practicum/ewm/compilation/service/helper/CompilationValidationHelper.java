package ru.practicum.ewm.compilation.service.helper;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.dto.exception.NotFoundException;

import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class CompilationValidationHelper {
    private final CompilationRepository compilationRepository;

    public Compilation isCompilationPresent(Long compilationId) {
        Optional<Compilation> optionalCompilation = compilationRepository.findById(compilationId);

        if (optionalCompilation.isEmpty()) {
            log.error("Подборка событий с ИД {} отсутствует в БД.", compilationId);
            throw new NotFoundException(String.format("Подборка событий с ИД %d отсутствует в БД.", compilationId));
        }

        return optionalCompilation.get();
    }
}
