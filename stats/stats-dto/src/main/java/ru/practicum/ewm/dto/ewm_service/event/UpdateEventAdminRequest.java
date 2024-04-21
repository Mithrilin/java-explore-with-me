package ru.practicum.ewm.dto.ewm_service.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import ru.practicum.ewm.dto.ewm_service.event.enums.UpdateEventStates;
import ru.practicum.ewm.dto.ewm_service.location.LocationDto;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventAdminRequest {
    @Length(min = 20, max = 2000, message = "Длина email должна быть в диапазоне от 20 до 2000 символов.")
    private String annotation;
    private Long category;
    @Length(min = 20, max = 7000, message = "Длина email должна быть в диапазоне от 20 до 7000 символов.")
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private LocationDto location;
    private boolean paid;
    private Integer participantLimit;
    private boolean requestModeration;
    private UpdateEventStates stateAction;
    @Length(min = 3, max = 120, message = "Длина email должна быть в диапазоне от 2 до 120 символов.")
    private String title;
}
