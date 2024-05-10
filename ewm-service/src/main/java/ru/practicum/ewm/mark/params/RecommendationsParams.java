package ru.practicum.ewm.mark.params;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendationsParams {
    private Map<Long, HashMap<Long, Integer>> userIdToEventIdWithDiff;
    private Map<Long, HashMap<Long, Integer>> userIdToEventIdWithMarkValue;
    private Map<Long, Integer> userIdToMatch;
    private long requesterId;
}
