package ru.practicum.ewm.dto.ewm_service.event.update_event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.dto.ewm_service.event.enums.UpdateEventAdminStates;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventAdminRequest extends UpdateEventRequest {
    private UpdateEventAdminStates stateAction;
}
