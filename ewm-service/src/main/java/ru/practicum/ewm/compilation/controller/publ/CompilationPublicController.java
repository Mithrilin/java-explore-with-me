package ru.practicum.ewm.compilation.controller.publ;

import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.compilation.service.CompilationService;
import ru.practicum.ewm.dto.ewm_service.compilation.CompilationDto;

import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/compilations")
@Validated
public class CompilationPublicController {
    private final CompilationService compilationService;

    @GetMapping
    public List<CompilationDto> getAllCompilations(@RequestParam(required = false) Boolean pinned,
                                                   @RequestParam(defaultValue = "0") @Min(0) int from,
                                                   @RequestParam(defaultValue = "10") @Positive int size) {
        return compilationService.getAllCompilations(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilationById(@PathVariable @Positive long compId) {
        return compilationService.getCompilationById(compId);
    }
}
