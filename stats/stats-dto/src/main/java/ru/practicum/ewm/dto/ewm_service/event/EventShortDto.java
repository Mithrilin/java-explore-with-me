package ru.practicum.ewm.dto.ewm_service.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.dto.ewm_service.category.CategoryDto;
import ru.practicum.ewm.dto.ewm_service.user.UserShortDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventShortDto {
    private Long id;
    @NotBlank(message = "annotation не может быть пустым.")
    private String annotation;
    private CategoryDto category;
    private Long confirmedRequests;
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private UserShortDto initiator;
    @NotNull
    private boolean paid;
    @NotBlank(message = "title не может быть пустым.")
    private String title;
    private Long views;
}
