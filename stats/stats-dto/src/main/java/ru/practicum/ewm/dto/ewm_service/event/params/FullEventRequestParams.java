package ru.practicum.ewm.dto.ewm_service.event.params;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.ewm.dto.ewm_service.event.enums.EventLifecycleStates;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FullEventRequestParams {
    private List<Long> users;
    private List<EventLifecycleStates> states;
    private List<Long> categories;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeStart;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeEnd;
    private Integer from;
    private Integer size;
    private Integer page;
    private Sort sort;
    private PageRequest pageRequest;

    public FullEventRequestParams(List<Long> users,
                                  List<EventLifecycleStates> states,
                                  List<Long> categories,
                                  LocalDateTime rangeStart,
                                  LocalDateTime rangeEnd,
                                  Integer from,
                                  Integer size) {
        this.users = users;
        this.states = states;
        this.categories = categories;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        this.from = from;
        this.size = size;
        this.page = from / size;
        this.sort = Sort.by(Sort.Direction.ASC, "id");
        this.pageRequest = PageRequest.of(page, size, sort);
    }
}
