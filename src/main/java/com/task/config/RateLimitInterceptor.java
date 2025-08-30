package com.task.config;

import com.task.annotation.RateLimited;
import com.task.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    public RateLimitInterceptor(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        String clientIp = getClientIp(request);
        String endpoint = request.getRequestURI();
        
        // Check for method-level @RateLimited annotation
        RateLimited rateLimited = handlerMethod.getMethodAnnotation(RateLimited.class);
        
        // If not found on method, check class level
        if (rateLimited == null) {
            rateLimited = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), RateLimited.class);
        }
        
        // Apply rate limiting with custom values if annotation is present
        if (rateLimited != null) {
            String rateLimitKey = rateLimited.value().isEmpty() ? endpoint : rateLimited.value();
            rateLimitService.checkRateLimit(
                clientIp, 
                rateLimitKey,
                rateLimited.requests(),
                rateLimited.duration()
            );
        } else {
            // Apply default rate limiting
            rateLimitService.checkRateLimit(clientIp, endpoint);
        }
        
        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
