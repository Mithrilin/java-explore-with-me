package ru.practicum.ewm.dto.ewm_service.event.update_event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.dto.ewm_service.event.enums.UpdateEventUserStates;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventUserRequest extends UpdateEventRequest {
    private UpdateEventUserStates stateAction;
}
