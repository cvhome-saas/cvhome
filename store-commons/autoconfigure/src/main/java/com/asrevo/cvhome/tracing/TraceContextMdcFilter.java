package com.asrevo.cvhome.tracing;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;

/**
 * Puts the current request's OpenTelemetry trace and span ids into the SLF4J MDC for the length of the request.
 *
 * <p>
 * The OpenTelemetry starter makes the server span current on the request thread but never touches the MDC; the
 * logback MDC appender it installs adds the ids to a copy of each log event for the console pattern, which code
 * reading {@code MDC.get(...)} cannot see. {@code ProblemDetailFactory} needs the real MDC to put the trace id on an error
 * response, so this filter — first after the span is started — writes {@code trace_id} and {@code span_id}, the same
 * keys the appender uses and Loki indexes, and removes them when the request is done.
 * </p>
 */
public class TraceContextMdcFilter extends OncePerRequestFilter {

    /**
     * MDC key of the W3C trace id, as the OpenTelemetry logback instrumentation names it.
     */
    public static final String TRACE_ID = "trace_id";

    /**
     * MDC key of the current span id.
     */
    public static final String SPAN_ID = "span_id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        SpanContext context = Span.current().getSpanContext();
        if (!context.isValid()) {
            chain.doFilter(request, response);
            return;
        }
        MDC.put(TRACE_ID, context.getTraceId());
        MDC.put(SPAN_ID, context.getSpanId());
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID);
            MDC.remove(SPAN_ID);
        }
    }

}
