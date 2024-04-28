package ru.practicum.ewm.dto.ewm_service.event.update_event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import ru.practicum.ewm.dto.ewm_service.location.LocationDto;

import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventRequest {
    @Length(min = 20, max = 2000, message = "Длина annotation должна быть в диапазоне от 20 до 2000 символов.")
    private String annotation;
    private Long category;
    @Length(min = 20, max = 7000, message = "Длина description должна быть в диапазоне от 20 до 7000 символов.")
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private LocationDto location;
    private Boolean paid;
    @PositiveOrZero
    private Integer participantLimit;
    private Boolean requestModeration;
    @Length(min = 3, max = 120, message = "Длина title должна быть в диапазоне от 3 до 120 символов.")
    private String title;
}
