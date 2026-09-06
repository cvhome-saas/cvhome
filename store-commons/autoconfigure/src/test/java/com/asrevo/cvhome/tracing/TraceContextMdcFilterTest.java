package com.asrevo.cvhome.tracing;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Scope;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The ids of the current span are in the MDC while the request runs and gone afterwards; without a span the MDC is
 * left alone.
 */
class TraceContextMdcFilterTest {

    private static final String TRACE = "4bf92f3577b34da6a3ce929d0e0e4736";

    private static final String SPAN = "00f067aa0ba902b7";

    private final TraceContextMdcFilter filter = new TraceContextMdcFilter();

    private Map<String, String> seen;

    private MockFilterChain capturingChain() {
        return new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) {
                seen = new HashMap<>();
                seen.put(TraceContextMdcFilter.TRACE_ID, MDC.get(TraceContextMdcFilter.TRACE_ID));
                seen.put(TraceContextMdcFilter.SPAN_ID, MDC.get(TraceContextMdcFilter.SPAN_ID));
            }
        };
    }

    @Test
    void theCurrentSpanIdsAreInTheMdcDuringTheRequestAndRemovedAfter() throws Exception {
        Span span = Span.wrap(SpanContext.create(TRACE, SPAN, TraceFlags.getSampled(), TraceState.getDefault()));
        try (Scope ignored = span.makeCurrent()) {
            filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), capturingChain());
        }

        assertThat(seen).containsEntry(TraceContextMdcFilter.TRACE_ID, TRACE)
                .containsEntry(TraceContextMdcFilter.SPAN_ID, SPAN);
        assertThat(MDC.get(TraceContextMdcFilter.TRACE_ID)).isNull();
        assertThat(MDC.get(TraceContextMdcFilter.SPAN_ID)).isNull();
    }

    @Test
    void withoutASpanTheMdcIsUntouched() throws Exception {
        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), capturingChain());

        assertThat(seen.get(TraceContextMdcFilter.TRACE_ID)).isNull();
        assertThat(seen.get(TraceContextMdcFilter.SPAN_ID)).isNull();
    }

}
