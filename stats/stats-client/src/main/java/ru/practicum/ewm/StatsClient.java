package ru.practicum.ewm;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StatsClient {
    private final RestTemplate restTemplate;

    @Value("${ewm.stats-server.url}")
    private final String statsServerUrl;

    @Retryable(
            retryFor = {RestClientException.class, IOException.class},
            maxAttempts = 6,
            backoff = @Backoff(delay = 1000, multiplier = 1.5)
    )
    public ResponseEntity<HitDto> createHit(CreateHitDto createDto) {
            return restTemplate.exchange(
                    statsServerUrl + "/hit",
                    HttpMethod.POST,
                    new HttpEntity<>(createDto, defaultHeaders()),
                    HitDto.class);
    }

    @Retryable(
            retryFor = {RestClientException.class, IOException.class},
            maxAttempts = 6,
            backoff = @Backoff(delay = 1000, multiplier = 1.5)
    )
    public ResponseEntity<List<ViewStatsDto>> getStats(String start, String end, List<String> uris, Boolean unique) {
        String uri = UriComponentsBuilder
                .fromUriString("/stats")
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParam("uris", uris != null ? uris.toArray() : new String[0])
                .queryParam("unique", unique)
                .build()
                .toUriString();

        HttpEntity<Void> requestEntity = new HttpEntity<>(defaultHeaders());

        return restTemplate.exchange(
                statsServerUrl + uri,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<>() {
                });
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}
