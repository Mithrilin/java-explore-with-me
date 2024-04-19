package ru.practicum.ewm.dto.ewm_service.compilation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.dto.ewm_service.event.EventShortDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompilationDto {
    @NotNull
    private Long id;
    private Set<EventShortDto> events;
    @NotNull
    private Boolean pinned;
    @NotBlank(message = "title не может быть пустым.")
    private String title;
}
