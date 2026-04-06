package com.nitro.tech.cloud.security;

import com.nitro.tech.cloud.config.ApiKeyProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final ApiKeyProperties apiKeyProperties;

    public ApiKeyAuthFilter(ApiKeyProperties apiKeyProperties) {
        this.apiKeyProperties = apiKeyProperties;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        String path = pathWithinApplication(request);
        if (HttpMethod.GET.matches(request.getMethod()) && path.equals("/health")) {
            return true;
        }
        if (HttpMethod.POST.matches(request.getMethod()) && path.equals("/auth/telegram")) {
            return true;
        }
        // SpringDoc: HTML under /swagger-ui, JS/CSS often under /webjars (must allow both)
        return path.startsWith("/swagger-ui")
                || path.equals("/swagger-ui.html")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/webjars/");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String provided = request.getHeader(apiKeyProperties.getHeaderName());
        if (!constantTimeEquals(apiKeyProperties.getKey(), provided)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"Invalid or missing API key\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private static boolean constantTimeEquals(String expected, String actual) {
        byte[] a = expected.getBytes(StandardCharsets.UTF_8);
        byte[] b = (actual == null ? "" : actual).getBytes(StandardCharsets.UTF_8);
        if (a.length != b.length) {
            return false;
        }
        return MessageDigest.isEqual(a, b);
    }

    /** Path for matching rules; strips {@code context-path} so it matches {@code WebSecurityCustomizer}. */
    private static String pathWithinApplication(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            uri = uri.substring(ctx.length());
        }
        return uri.isEmpty() ? "/" : uri;
    }
}
