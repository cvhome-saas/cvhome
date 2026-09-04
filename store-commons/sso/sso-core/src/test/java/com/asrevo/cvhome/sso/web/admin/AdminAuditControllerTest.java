package com.asrevo.cvhome.sso.web.admin;

import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;

import com.asrevo.cvhome.sso.audit.AuditEventType;
import com.asrevo.cvhome.sso.audit.AuditQueryService;
import com.asrevo.cvhome.sso.audit.AuditSearch;
import com.asrevo.cvhome.sso.dto.AuditEventDto;
import com.asrevo.cvhome.sso.dto.AuditQueryParams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The audit log, read.
 *
 * <p>
 * Nothing here writes a row: every audit event is a side effect of the action it describes, so a write path on this
 * controller would let an operator forge history.
 * </p>
 *
 * <p>
 * The export is a GET because a browser downloads it from a plain link, which means the session — not a bearer
 * token — is what authorises it, and the writer is closed by the try-with-resources whether or not the query
 * throws. A half-written CSV that ends without an error is worse than no file.
 * </p>
 */
class AdminAuditControllerTest {

    private static final long EVENT_ID = 42L;

    private final AuditQueryService audit = mock(AuditQueryService.class);
    private final AdminAuditController controller = new AdminAuditController(audit);

    @Test
    void thesearchPassesTheConsolesQueryThroughAsAsearch() throws Exception {
        AuditQueryParams params = mock(AuditQueryParams.class);
        AuditSearch search = mock(AuditSearch.class);
        Pageable pageable = PageRequest.of(0, 50);
        when(params.toSearch()).thenReturn(search);
        when(audit.search(search, pageable)).thenReturn(new PageImpl<>(List.of()));

        assertThat(controller.search(params, pageable)).isEmpty();
        verify(audit).search(search, pageable);
    }

    @Test
    void theTypeCatalogueIsTheServersOwnEnumeration() {
        assertThat(controller.types()).hasSize(AuditEventType.values().length).isNotEmpty();
    }

    @Test
    void asingleEventDelegatesStraightThrough() throws Exception {
        AuditEventDto event = mock(AuditEventDto.class);
        when(audit.findOne(EVENT_ID)).thenReturn(event);

        assertThat(controller.findOne(EVENT_ID)).isSameAs(event);
    }

    @Test
    void theExportIsAcsvAttachmentWrittenStraightToTheResponse() throws Exception {
        AuditQueryParams params = mock(AuditQueryParams.class);
        AuditSearch search = mock(AuditSearch.class);
        when(params.toSearch()).thenReturn(search);
        doAnswer(invocation -> {
            invocation.getArgument(1, Writer.class).write("id,occurredAt\n1,2026-01-01T00:00:00Z\n");
            return 1L;
        }).when(audit).exportCsv(any(AuditSearch.class), any(Writer.class));
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.export(params, response);

        assertThat(response.getContentType()).startsWith("text/csv");
        assertThat(response.getHeader(HttpHeaders.CONTENT_DISPOSITION)).contains("attachment").contains(".csv");
        assertThat(response.getContentAsString()).contains("occurredAt");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("endpoints")
    void everyEndpointOnThisControllerIsThePlatformOperatorsAlone(Method endpoint) {
        PreAuthorize gate = endpoint.getAnnotation(PreAuthorize.class);

        assertThat(gate).as("%s has no @PreAuthorize", endpoint.getName()).isNotNull();
        assertThat(gate.value()).contains("super_admin").contains("SUPER_ADMIN");
    }

    private static Stream<Method> endpoints() {
        return Stream.of(AdminAuditController.class.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .sorted((a, b) -> a.getName().compareTo(b.getName()));
    }

}
