package ru.practicum.ewm.compilation.controller.admin;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.compilation.service.CompilationService;
import ru.practicum.ewm.dto.ewm_service.compilation.CompilationDto;
import ru.practicum.ewm.dto.ewm_service.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.ewm_service.compilation.UpdateCompilationRequest;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@AllArgsConstructor
@RestController
@RequestMapping("/admin/compilations")
@Validated
public class CompilationAdminController {
    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto createCompilation(@RequestBody @Valid NewCompilationDto newCompilationDto) {
        return compilationService.addCompilation(newCompilationDto);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable @Positive long compId) {
        compilationService.deleteCompilation(compId);
    }

    @PatchMapping("/{compId}")
    public UpdateCompilationRequest updateCompilationById(@PathVariable @Positive long compId) {
        return compilationService.updateCompilationById(compId);
    }
}
