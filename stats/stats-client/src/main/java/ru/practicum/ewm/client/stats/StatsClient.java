package ru.practicum.ewm.client.stats;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.dto.stats.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class StatsClient {
    private final WebClient webClient;

    public StatsClient(@Value("${stats-server.url}") String statsServerUrl) {
        this.webClient = WebClient.create(statsServerUrl);
    }

    public EndpointHit addHitStats(EndpointHit endpointHit) {
        return webClient
                .post()
                .uri("/hit")
                .bodyValue(endpointHit)
                .retrieve()
                .bodyToMono(EndpointHit.class)
                .doOnSuccess(e -> log.info("Статистические данные успешно сохранены."))
                .doOnError(error -> log.error("Сервер статистики не отвечает. Информация по данному запросу не " +
                        "сохранена в статистику! ErrorMessage: {}", error.getMessage()))
                .block();
    }

    public List<ViewStats> getViewStatsList(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> parameters = new HashMap<>(Map.of(
                "start", start.format(formatter),
                "end", end.format(formatter),
                "unique", unique
        ));

        String path;
        if (uris == null) {
            path = "/stats?start={start}&end={end}&unique={unique}";
        } else {
            parameters.put("uris", String.join(",", uris));
            path = "/stats?start={start}&end={end}&uris={uris}&unique={unique}";
        }

        return webClient
                .get()
                .uri(path, parameters)

                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ViewStats>>() {})
                .doOnError(error -> log.error("Сервер статистики не отвечает. Информация по данному запросу не " +
                        "сохранена в статистику! ErrorMessage: {}", error.getMessage()))
                .block();
    }
}
