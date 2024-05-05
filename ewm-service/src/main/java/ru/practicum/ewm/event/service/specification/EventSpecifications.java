package ru.practicum.ewm.event.service.specification;

import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.ewm.dto.ewm_service.event.enums.EventLifecycleStates;
import ru.practicum.ewm.dto.ewm_service.event.params.FullEventRequestParams;
import ru.practicum.ewm.dto.ewm_service.event.params.ShortEventRequestParams;
import ru.practicum.ewm.event.model.Event;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@UtilityClass
public class EventSpecifications {

    public static List<Specification<Event>> searchFullEventFilterToSpecifications(FullEventRequestParams params) {
        List<Specification<Event>> specifications = new ArrayList<>();
        specifications.add(params.getUsers() == null ? null : userIdIn(params.getUsers()));
        specifications.add(params.getStates() == null ? null : statesIn(params.getStates()));
        specifications.add(params.getCategories() == null ? null : categoryIdIn(params.getCategories()));
        specifications.add(params.getRangeStart() == null ? null : eventDateAfter(params.getRangeStart()));
        specifications.add(params.getRangeEnd() == null ? null : eventDateBefore(params.getRangeEnd()));
        return specifications.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static List<Specification<Event>> searchShortEventFilterToSpecifications(ShortEventRequestParams params) {
        List<Specification<Event>> specifications = new ArrayList<>();
        specifications.add(stateEqual());
        specifications.add(params.getText() == null ? null : annotationOrDescriptionLike(params.getText().toLowerCase()));
        specifications.add(params.getCategories() == null ? null : categoryIdIn(params.getCategories()));
        specifications.add(params.getPaid() == null ? null : paidEqual(params.getPaid()));

        if (params.getRangeStart() == null && params.getRangeEnd() == null) {
            specifications.add(eventDateAfterNow());
        } else {
            specifications.add(params.getRangeStart() == null ? null : eventDateAfter(params.getRangeStart()));
            specifications.add(params.getRangeEnd() == null ? null : eventDateBefore(params.getRangeEnd()));
        }

        specifications.add(params.getOnlyAvailable() == null ? null : confirmedRequestsLessThan(params.getOnlyAvailable()));
        return specifications.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private static Specification<Event> annotationOrDescriptionLike(String values) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), values),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), values)
        );
    }

    private static Specification<Event> stateEqual() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("state"), EventLifecycleStates.PUBLISHED);
    }

    private static Specification<Event> paidEqual(Boolean values) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("paid"), values);
    }

    private static Specification<Event> confirmedRequestsLessThan(Boolean onlyAvailable) {
        if (onlyAvailable) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get("confirmedRequests"),
                    root.get("participantLimit"));
        } else {
            return null;
        }
    }

    private static Specification<Event> userIdIn(List<Long> values) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.in(root.get("initiator").get("id")).value(values);
    }

    private static Specification<Event> statesIn(List<EventLifecycleStates> values) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.in(root.get("state")).value(values);
    }

    private static Specification<Event> categoryIdIn(List<Long> values) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.in(root.get("category").get("id")).value(values);
    }

    private static Specification<Event> eventDateAfter(LocalDateTime values) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), values);
    }

    private static Specification<Event> eventDateAfterNow() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"),
                LocalDateTime.now());
    }

    private static Specification<Event> eventDateBefore(LocalDateTime values) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), values);
    }
}
