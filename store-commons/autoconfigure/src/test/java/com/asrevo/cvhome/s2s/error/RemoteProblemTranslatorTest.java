package com.asrevo.cvhome.s2s.error;

import java.net.ConnectException;
import java.net.URI;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.asrevo.cvhome.errors.RemoteServiceException;
import com.asrevo.cvhome.errors.remote.RemoteErrorCatalog;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A failed service-to-service call becomes a {@link RemoteServiceException} naming the service, the status and the
 * remote code — from a problem document, from a body that is not one, and from no response at all.
 */
class RemoteProblemTranslatorTest {

    private static final URI URI_CALLED = URI.create("http://catalog:8122/api/v1/products/7");

    private static final String CATALOG = "catalog";

    @Test
    void aProblemDocumentKeepsItsCodeAndStatus() {
        RemoteServiceException e = RemoteProblemTranslator.translate(RemoteErrorCatalog.none(), URI_CALLED,
                HttpStatus.NOT_FOUND, "{\"code\":\"CATALOG.PRODUCT.NOT_FOUND\",\"status\":404,\"detail\":\"gone\"}");

        assertThat(e.remoteService()).isEqualTo(CATALOG);
        assertThat(e.remoteStatus()).isEqualTo(404);
        assertThat(e.remoteCode()).isEqualTo("CATALOG.PRODUCT.NOT_FOUND");
    }

    @Test
    void aBodyThatIsNotAProblemDocumentStillTranslates() {
        RemoteServiceException html = RemoteProblemTranslator.translate(RemoteErrorCatalog.none(), URI_CALLED,
                HttpStatus.BAD_GATEWAY, "<html>502</html>");
        RemoteServiceException empty = RemoteProblemTranslator.translate(RemoteErrorCatalog.none(), URI_CALLED,
                HttpStatus.INTERNAL_SERVER_ERROR, "");

        assertThat(html.remoteService()).isEqualTo(CATALOG);
        assertThat(html.remoteStatus()).isEqualTo(502);
        assertThat(empty.remoteStatus()).isEqualTo(500);
    }

    @Test
    void noResponseAtAllNamesTheServiceThatCouldNotBeReached() {
        RemoteServiceException e = RemoteProblemTranslator.unreachable(RemoteErrorCatalog.none(), URI_CALLED,
                new ConnectException("refused"));

        assertThat(e.remoteService()).isEqualTo(CATALOG);
        assertThat(e).hasCauseInstanceOf(ConnectException.class);
    }

}
