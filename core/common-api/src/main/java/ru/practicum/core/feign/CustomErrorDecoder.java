package ru.practicum.core.feign;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.core.exception.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom Feign error decoder that converts HTTP error responses (4xx, 5xx)
 * into typed exceptions with messages extracted from JSON body.
 *
 * <p>Logging example:
 * <br>{@code WARN [RequestFeignClient] Conflict: Event 479 has reached participant limit}
 *
 * <p>Exceptions are handled by {@link ExceptionController}.
 */
@Slf4j
public class CustomErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        String clientName = extractClientName(methodKey);
        String message = parseMessage(getBody(response));

        if (status == 400) {
            log.warn("[{}] Bad Request: {}", clientName, message);
            throw new BadRequestException(message);
        }

        if (status == 403) {
            log.warn("[{}] Access denied: {}", clientName, message);
            throw new AccessException(message);
        }

        if (status == 404) {
            log.warn("[{}] Not Found: {}", clientName, message);
            throw new NotFoundException(message);
        }

        if (status == 409) {
            log.warn("[{}] Conflict: {}", clientName, message);
            throw new ConflictException(message);
        }

        if (status >= 500 && status < 600) {
            log.error("[{}] Server Error {}: {}", clientName, status, message);
            throw new ServiceUnavailableException(message);
        }

        return defaultDecoder.decode(methodKey, response);
    }

    private String extractClientName(String methodKey) {
        if (methodKey != null && methodKey.contains("#")) {
            return methodKey.split("#")[0];
        }
        return methodKey;
    }

    private String parseMessage(String body) {
        try {
            JsonNode jsonNode = objectMapper.readTree(body);

            if (jsonNode.has("errors") && jsonNode.get("errors").isArray()) {
                JsonNode errorsArray = jsonNode.get("errors");
                if (errorsArray.size() == 1) {
                    return errorsArray.get(0).asText();
                }

                List<String> errorList = new ArrayList<>();
                for (JsonNode error : errorsArray) {
                    errorList.add(error.asText());
                }
                return String.join(", ", errorList);
            }

            if (jsonNode.has("message")) {
                return jsonNode.get("message").asText();
            }

            return body;

        } catch (Exception e) {
            return body;
        }
    }

    private String getBody(Response response) {
        try {
            if (response.body() != null) {
                byte[] bytes = response.body().asInputStream().readAllBytes();
                return new String(bytes);
            }
        } catch (Exception e) {
            log.debug("Failed to read response body", e);
        }
        return "no body";
    }
}