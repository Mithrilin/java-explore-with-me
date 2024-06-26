package ru.practicum.ewm.dto.stats;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EndpointHit {
    @NotBlank(message = "Идентификатор сервиса не может быть пустым.")
    private String app;
    @NotBlank(message = "URI не может быть пустым.")
    private String uri;
    @NotBlank(message = "IP-адрес пользователя не может быть пустым.")
    private String ip;
    @NotNull(message = "Дата и время запроса не могут быть пустыми.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
