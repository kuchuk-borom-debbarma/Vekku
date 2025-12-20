package dev.kbd.vekku_server.infra.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitingService rateLimitingService;

    private static final int DEFAULT_LIMIT = 50;
    private static final int DEFAULT_DURATION = 60; // 1 minute

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true; // Skip for static resources etc.
        }

        // Determine limits
        int limit = DEFAULT_LIMIT;
        int duration = DEFAULT_DURATION;

        // Check Method Annotation
        if (handlerMethod.hasMethodAnnotation(RateLimit.class)) {
            RateLimit annotation = handlerMethod.getMethodAnnotation(RateLimit.class);
            limit = annotation.limit();
            duration = annotation.duration();
        }
        // Check Class Annotation (if not on method)
        else if (handlerMethod.getBeanType().isAnnotationPresent(RateLimit.class)) {
            RateLimit annotation = handlerMethod.getBeanType().getAnnotation(RateLimit.class);
            limit = annotation.limit();
            duration = annotation.duration();
        }

        String clientIp = getClientIp(request);
        // Key concept: IP + Endpoint combo? Or just IP?
        // Usually, global limit is per IP.
        // Specific endpoint limit is per IP + Endpoint.

        // Let's make key unique per limit-configuration.
        // If annotated, key = IP + MethodName.
        // If default, key = IP + "GLOBAL".

        String key;
        if (handlerMethod.hasMethodAnnotation(RateLimit.class)
                || handlerMethod.getBeanType().isAnnotationPresent(RateLimit.class)) {
            key = clientIp + ":" + request.getRequestURI();
        } else {
            key = clientIp + ":GLOBAL";
        }

        Bucket bucket = rateLimitingService.resolveBucket(key, limit, duration);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (!probe.isConsumed()) {
            log.warn("Rate limit exceeded for key: {}", key);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too many requests");
            return false;
        }

        response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
