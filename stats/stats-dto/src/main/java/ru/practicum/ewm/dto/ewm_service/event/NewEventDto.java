package ru.practicum.ewm.dto.ewm_service.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import ru.practicum.ewm.dto.ewm_service.location.LocationDto;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewEventDto {
    @NotBlank(message = "annotation не может быть пустым.")
    @Length(min = 20, max = 2000, message = "Длина name должна быть в диапазоне от 20 до 2000 символов.")
    private String annotation;
    @NotNull
    private Long category;
    @NotBlank(message = "description не может быть пустым.")
    @Length(min = 20, max = 7000, message = "Длина name должна быть в диапазоне от 20 до 7000 символов.")
    private String description;
    @NotNull
    @Future
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    @NotNull
    private LocationDto location;
    private Boolean paid = false;
    private Integer participantLimit = 0;
    private Boolean requestModeration = true;
    @NotBlank(message = "title не может быть пустым.")
    @Length(min = 3, max = 120, message = "Длина name должна быть в диапазоне от 3 до 120 символов.")
    private String title;
}
