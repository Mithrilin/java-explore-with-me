package ru.practicum.ewm.dto.ewm_service.event.params;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.ewm.dto.ewm_service.event.enums.EventSortAvailableValues;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ShortEventRequestParams {
    private String text;
    private List<Long> categories;
    private Boolean paid;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeStart;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeEnd;
    private Boolean onlyAvailable;
    private Sort sort;
    private Integer from;
    private Integer size;
    private Integer page;
    private PageRequest pageRequest;

    public ShortEventRequestParams(String text,
                                   List<Long> categories,
                                   Boolean paid,
                                   LocalDateTime rangeStart,
                                   LocalDateTime rangeEnd,
                                   Boolean onlyAvailable,
                                   EventSortAvailableValues sort,
                                   Integer from,
                                   Integer size) {
        this.text = text;
        this.categories = categories;
        this.paid = paid;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        this.onlyAvailable = onlyAvailable;
        this.from = from;
        this.size = size;
        this.page = from / size;
        this.sort = sort == EventSortAvailableValues.VIEWS ? Sort.by(Sort.Direction.DESC, "views")
                : sort == EventSortAvailableValues.RATING ? Sort.by(Sort.Direction.DESC, "rating")
                : Sort.by(Sort.Direction.ASC, "eventDate");
        this.pageRequest = PageRequest.of(page, size, this.sort);
    }
}
