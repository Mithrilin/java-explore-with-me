package ru.practicum.ewm.dto.ewm_service.compilation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCompilationRequest {
    private Set<Long> events;
    private boolean pinned;
    @Length(min = 1, max = 50, message = "Длина email должна быть в диапазоне от 1 до 50 символов.")
    private String title;
}
