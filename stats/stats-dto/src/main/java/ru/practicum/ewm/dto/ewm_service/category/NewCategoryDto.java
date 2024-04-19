package ru.practicum.ewm.dto.ewm_service.category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewCategoryDto {
    @NotBlank(message = "Name не может быть пустым.")
    @Length(min = 1, max = 50, message = "Длина name должна быть в диапазоне от 1 до 50 символов.")
    private String name;
}
