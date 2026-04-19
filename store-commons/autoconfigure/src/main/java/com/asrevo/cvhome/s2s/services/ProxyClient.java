package com.asrevo.cvhome.s2s.services;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.reactivestreams.Publisher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

public record ProxyClient(WebClient storeProxyClient) {

    public <T> Mono<ResponseEntity<T>> send(SendRequest<T> request) {
        Objects.requireNonNull(request, "request must not be null");

        WebClient.RequestBodySpec bodySpec = storeProxyClient.method(request.method)
                .uri(request.uri)
                .headers(httpHeaders -> request.headers.forEach(httpHeaders::add));

        WebClient.RequestHeadersSpec<?> headersSpec = request.hasBody
                ? bodySpec.body(request.bodyPublisher, request.bodyElementTypeRef) : bodySpec;

        return headersSpec.exchangeToMono(request.responseMapper);
    }

    public static final class SendRequest<T> {

        private final HttpMethod method;

        private final String uri;

        private final Map<String, String> headers;

        private final Publisher<?> bodyPublisher; // optional

        private final ParameterizedTypeReference<?> bodyElementTypeRef; // optional

        private final Function<ClientResponse, Mono<ResponseEntity<T>>> responseMapper;

        private final boolean hasBody;

        private SendRequest(Builder<T> b) {
            this.method = b.method;
            this.uri = b.uri;
            this.headers = b.headers;
            this.bodyPublisher = b.bodyPublisher;
            this.bodyElementTypeRef = b.bodyElementTypeRef;
            this.responseMapper = b.responseMapper;
            this.hasBody = b.hasBody;
        }

        public static <T> Builder<T> builder(HttpMethod method,
                                             Function<ClientResponse, Mono<ResponseEntity<T>>> responseMapper) {
            return new Builder<>(method, responseMapper);
        }

        public static final class Builder<T> {

            private final HttpMethod method;

            private final Function<ClientResponse, Mono<ResponseEntity<T>>> responseMapper;

            private String uri = "";

            private Map<String, String> headers = Map.of();

            private Publisher<?> bodyPublisher;

            private ParameterizedTypeReference<?> bodyElementTypeRef;

            private boolean hasBody = false;

            private Builder(HttpMethod method, Function<ClientResponse, Mono<ResponseEntity<T>>> responseMapper) {
                this.method = Objects.requireNonNull(method, "HTTP method must not be null");
                this.responseMapper = Objects.requireNonNull(responseMapper, "responseMapper must not be null");
            }

            public Builder<T> uri(String uri) {
                this.uri = (uri == null) ? "" : uri;
                return this;
            }

            public Builder<T> headers(Map<String, String> headers) {
                this.headers = (headers == null) ? Map.of() : headers;
                return this;
            }

            public <P> Builder<T> body(Publisher<P> publisher, ParameterizedTypeReference<P> elementTypeRef) {
                Objects.requireNonNull(publisher, "Publisher must not be null");
                Objects.requireNonNull(elementTypeRef, "ElementTypeRef must not be null");
                this.bodyPublisher = publisher;
                this.bodyElementTypeRef = elementTypeRef;
                this.hasBody = true;
                return this;
            }

            public SendRequest<T> build() {
                return new SendRequest<>(this);
            }

        }

    }
}
