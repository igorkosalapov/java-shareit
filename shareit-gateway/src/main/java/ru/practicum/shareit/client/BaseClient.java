package ru.practicum.shareit.client;

import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class BaseClient {
    public static final String USER_HEADER = "X-Sharer-User-Id";

    private final RestTemplate restTemplate;
    private final String serverUrl;

    public BaseClient(RestTemplate restTemplate, String serverUrl) {
        this.restTemplate = restTemplate;
        this.serverUrl = serverUrl;
    }

    protected ResponseEntity<Object> get(String path, long userId) {
        return exchange(buildUri(path, null), HttpMethod.GET, new HttpEntity<>(headers(userId)));
    }

    protected ResponseEntity<Object> get(String path, long userId, Map<String, Object> queryParams) {
        return exchange(buildUri(path, queryParams), HttpMethod.GET, new HttpEntity<>(headers(userId)));
    }

    protected ResponseEntity<Object> post(String path, long userId, Object body) {
        return exchange(buildUri(path, null), HttpMethod.POST, new HttpEntity<>(body, headers(userId)));
    }

    protected ResponseEntity<Object> patch(String path, long userId, @Nullable Object body) {
        return patch(path, userId, body, null);
    }

    protected ResponseEntity<Object> patch(String path, long userId) {
        return patch(path, userId, null, null);
    }

    protected ResponseEntity<Object> patch(String path, long userId, @Nullable Object body, @Nullable Map<String, Object> queryParams) {
        return exchange(buildUri(path, queryParams),
                HttpMethod.PATCH,
                new HttpEntity<>(body, headers(userId)));
    }

    protected ResponseEntity<Object> delete(String path, long userId) {
        return exchange(buildUri(path, null), HttpMethod.DELETE, new HttpEntity<>(headers(userId)));
    }

    protected ResponseEntity<Object> getTemplate(String pathTemplate, long userId, Map<String, Object> uriVars) {
        return exchange(buildTemplateUri(pathTemplate, uriVars), HttpMethod.GET, new HttpEntity<>(headers(userId)));
    }

    protected ResponseEntity<Object> patchTemplate(String pathTemplate, long userId,
                                                   @Nullable Object body, Map<String, Object> uriVars) {
        return exchange(buildTemplateUri(pathTemplate, uriVars), HttpMethod.PATCH, new HttpEntity<>(body, headers(userId)));
    }

    private ResponseEntity<Object> exchange(URI uri, HttpMethod method, HttpEntity<?> entity) {
        try {
            return restTemplate.exchange(uri, method, entity, Object.class);
        } catch (HttpStatusCodeException e) {

            return ResponseEntity
                    .status(e.getStatusCode())
                    .headers(safeHeaders(e))
                    .body(safeBody(e));
        }
    }

    private HttpHeaders headers(long userId) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.setAccept(MediaType.parseMediaTypes(MediaType.APPLICATION_JSON_VALUE));
        h.add(USER_HEADER, String.valueOf(userId));
        return h;
    }

    private URI buildUri(String path, @Nullable Map<String, Object> queryParams) {
        UriComponentsBuilder b = UriComponentsBuilder.fromHttpUrl(serverUrl).path(path);

        if (queryParams != null) {
            queryParams.forEach((k, v) -> {
                if (v != null) {
                    b.queryParam(k, v);
                }
            });
        }
        return b.build(true).toUri();
    }

    private URI buildTemplateUri(String pathTemplate, Map<String, Object> uriVars) {
        return UriComponentsBuilder.fromHttpUrl(serverUrl)
                .path(pathTemplate)
                .buildAndExpand(uriVars)
                .encode()
                .toUri();
    }

    private HttpHeaders safeHeaders(HttpStatusCodeException e) {
        HttpHeaders original = e.getResponseHeaders();
        HttpHeaders result = new HttpHeaders();
        if (original != null && original.getContentType() != null) {
            result.setContentType(original.getContentType());
        }
        return result;
    }

    private Object safeBody(HttpStatusCodeException e) {
        byte[] bytes = e.getResponseBodyAsByteArray();
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
