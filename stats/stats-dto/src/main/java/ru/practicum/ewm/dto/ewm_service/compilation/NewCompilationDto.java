package ru.practicum.ewm.dto.ewm_service.compilation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewCompilationDto {
    private Set<Long> events;
    private Boolean pinned = false;
    @NotBlank(message = "title не может быть пустым.")
    @Length(min = 1, max = 50, message = "Длина name должна быть в диапазоне от 1 до 50 символов.")
    private String title;
}
