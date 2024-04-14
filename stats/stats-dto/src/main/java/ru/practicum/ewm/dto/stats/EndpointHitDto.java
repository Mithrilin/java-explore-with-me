package ru.practicum.ewm.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EndpointHitDto {
    private Long id;
    @NotBlank(message = "Идентификатор сервиса не может быть пустым.")
    private String app;
    @NotBlank(message = "URI не может быть пустым.")
    private String uri;
    @NotBlank(message = "IP-адрес пользователя не может быть пустым.")
    private String ip;
    @NotNull(message = "Дата и время запроса не может быть пустым.")
    private LocalDateTime timestamp;
}
