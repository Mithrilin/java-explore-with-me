package ru.practicum.ewm.dto.ewm_service.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    @Email
    @NotBlank(message = "Email не может быть пустым.")
    private String email;
    @NotBlank(message = "Name не может быть пустым.")
    private String name;
}
