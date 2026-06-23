package com.cts.agrilink.client;

import com.cts.agrilink.auditLog.dto.AuditLogDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * HTTP client other modules/services use to write an audit entry by calling
 * POST /agriLink/auditLog on the IAM audit service.
 *
 * Behaviour: best-effort — failures are logged and swallowed so callers' business
 * logic is not impacted.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditClient {

    private static final String AUDIT_PATH = "/agriLink/auditLog";

    private final ObjectProvider<Object> iamWebClientProvider;

    /** Enum-typed overload — convenient when callers use enums. */
    public void record(Integer userId, Enum<?> action, Enum<?> module, String authorizationHeader) {
        record(userId, action == null ? null : action.name(), module == null ? null : module.name(), authorizationHeader);
    }

    /** Sends an audit entry and forwards the caller's Authorization header. */
    public void record(Integer userId, String action, String module, String authorizationHeader) {
        AuditLogDto body = new AuditLogDto();
        body.setUserId(userId);
        body.setAction(action);
        body.setModule(module);
        Object webClient = iamWebClientProvider == null ? null : iamWebClientProvider.getIfAvailable();
        if (webClient == null) {
            log.debug("No WebClient available — skipping audit POST (userId={}, action={}, module={})", userId, action, module);
            return;
        }

        try {
            // Use reflection so this class does not require WebClient at compile/class-load time
            Class<?> webClientClass = webClient.getClass();
            Object request = webClientClass.getMethod("post").invoke(webClient);
            request = request.getClass().getMethod("uri", String.class).invoke(request, AUDIT_PATH);
            request = request.getClass().getMethod("header", String.class, String.class)
                    .invoke(request, HttpHeaders.AUTHORIZATION, authorizationHeader == null ? "" : authorizationHeader);
            request = request.getClass().getMethod("contentType", MediaType.class).invoke(request, MediaType.APPLICATION_JSON);
            request = request.getClass().getMethod("bodyValue", Object.class).invoke(request, body);
            Object responseSpec = request.getClass().getMethod("retrieve").invoke(request);
            Object mono = responseSpec.getClass().getMethod("toBodilessEntity").invoke(responseSpec);
            // add doOnError then subscribe
            java.util.function.Consumer<Throwable> consumer = e -> log.warn("Failed to record audit entry (userId={}, action={}, module={}): {}", userId, action, module, e.getMessage());
            mono.getClass().getMethod("doOnError", java.util.function.Consumer.class).invoke(mono, consumer);
            mono.getClass().getMethod("subscribe").invoke(mono);
        } catch (RuntimeException ex) {
            log.warn("Failed to record audit entry (userId={}, action={}, module={}): {}", userId, action, module, ex.getMessage());
        } catch (Exception ex) {
            log.warn("Failed to record audit entry (reflection) (userId={}, action={}, module={}): {}", userId, action, module, ex.getMessage());
        }
    }

}
