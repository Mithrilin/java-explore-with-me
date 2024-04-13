package ru.practicum.ewm.stats.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.stats.service.StatsService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StatsController.class)
class StatsControllerTest {
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private StatsService statsService;

    @Test
    @DisplayName("Успешное добавление статистики.")
    void addHitStats_whenEndpointHitIsValid_thenReturnedEndpointHit() throws Exception {
        EndpointHit endpointHit = new EndpointHit("ewm-main-service", "/events", "121.0.0.1",
                LocalDateTime.of(2025, 4, 19, 9, 54, 34));
        when(statsService.addHitStats(Mockito.any(EndpointHit.class))).thenReturn(endpointHit);

        mvc.perform(post("/hit")
                        .content(mapper.writeValueAsString(endpointHit))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.app", is(endpointHit.getApp())))
                .andExpect(jsonPath("$.uri", is(endpointHit.getUri())))
                .andExpect(jsonPath("$.ip", is(endpointHit.getIp())))
                .andExpect(jsonPath("$.timestamp", is(endpointHit.getTimestamp()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))));
    }

    @Test
    @DisplayName("Успешное получение списка статистики если uris null")
    void getViewStatsList_whenUrisNull_thenReturnedViewStatsList() throws Exception {
        ViewStats viewStats = new ViewStats("ewm-main-service", "/events", 1L);
        List<ViewStats> viewStatsList = List.of(viewStats);
        when(statsService.getViewStatsListWithoutUris(Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class),
                Mockito.anyBoolean())).thenReturn(viewStatsList);

        mvc.perform(get("/stats")
                        .param("start", "2025-04-19 09:54:34")
                        .param("end", "2025-05-19 09:54:34")
                        .param("unique", "false")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].app", is(viewStats.getApp())))
                .andExpect(jsonPath("$[0].uri", is(viewStats.getUri())))
                .andExpect(jsonPath("$[0].hits", is(viewStats.getHits()), Long.class));
    }

    @Test
    @DisplayName("Успешное получение списка статистики если uris не null")
    void getViewStatsList_whenUrisNotNull_thenReturnedViewStatsList() throws Exception {
        ViewStats viewStats = new ViewStats("ewm-main-service", "/events", 1L);
        List<ViewStats> viewStatsList = List.of(viewStats);
        when(statsService.getViewStatsListWithUris(Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class),
                Mockito.any(), Mockito.anyBoolean())).thenReturn(viewStatsList);

        mvc.perform(get("/stats")
                        .param("start", "2025-04-19 09:54:34")
                        .param("end", "2025-05-19 09:54:34")
                        .param("uris", "/events,/events/5,/events")
                        .param("unique", "false")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].app", is(viewStats.getApp())))
                .andExpect(jsonPath("$[0].uri", is(viewStats.getUri())))
                .andExpect(jsonPath("$[0].hits", is(viewStats.getHits()), Long.class));
    }

    @Test
    @DisplayName("Получение пустого списка статистики, когда даты неправильные и если uris не null ")
    void getViewStatsList_whenUrisNotNullAndWrongStart_thenThrowNotValidException() throws Exception {
        mvc.perform(get("/stats")
                        .param("start", "2026-04-19 09:54:34")
                        .param("end", "2025-05-19 09:54:34")
                        .param("uris", "/events,/events/5,/events")
                        .param("unique", "false")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(statsService, never()).getViewStatsListWithUris(Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class),
                Mockito.any(), Mockito.anyBoolean());
    }

    @Test
    @DisplayName("Получение пустого списка статистики, когда даты неправильные и если uris null ")
    void getViewStatsList_whenUrisNullAndWrongStart_thenThrowNotValidException() throws Exception {
        mvc.perform(get("/stats")
                        .param("start", "2026-04-19 09:54:34")
                        .param("end", "2025-05-19 09:54:34")
                        .param("uris", "/events,/events/5,/events")
                        .param("unique", "false")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(statsService, never()).getViewStatsListWithoutUris(Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class),
                Mockito.anyBoolean());
    }
}