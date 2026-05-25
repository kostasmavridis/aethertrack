package com.aethertrack.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter that reads X-Correlation-ID from inbound requests
 * (or generates a fresh UUID) and stores it in CorrelationIdHolder
 * for the lifetime of the request thread.
 */
@Component
public class CorrelationIdFilter implements Filter {

    public static final String HEADER = "X-Correlation-ID";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) req;
        String correlationId = httpReq.getHeader(HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        CorrelationIdHolder.set(correlationId);
        try {
            chain.doFilter(req, res);
        } finally {
            CorrelationIdHolder.clear();
        }
    }
}
