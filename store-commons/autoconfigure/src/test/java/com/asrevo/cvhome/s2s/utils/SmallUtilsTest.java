package com.asrevo.cvhome.s2s.utils;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.ServiceDomain;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Two small helpers that sit on the wire and on the redirect path.
 *
 * <p>
 * {@link ObjectIdDeserializer} reads the object shape an {@code ObjectId} used to serialize as — {@code {"id":…}} —
 * which is what stored payloads written before the change still hold. It answers null rather than throwing for
 * anything it cannot read, because a stored row that cannot be deserialized is an event nobody ever handles.
 * </p>
 */
class SmallUtilsTest {

    private static final String HEX = "507f1f77bcf86cd799439011";
    private static final String SIGN_IN = "https://console.example.com:443/sign-in";
    private static final String ORIGIN = "https://console.example.com:443";

    private static RedirectionUrlBuilder consoleRedirects() {
        return new RedirectionUrlBuilder("https", 443,
                new ServiceDomain("console-ui", "console.example.com", "4200", "http", "ns", "gw"));
    }

    private record Holder(ObjectId id) {
    }

    private static ObjectMapper mapperWithDeserializer() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ObjectId.class, new ObjectIdDeserializer());
        return JsonMapper.builder().addModule(module).build();
    }

    @Test
    void theObjectShapeAStoredPayloadStillHoldsIsRead() {
        Holder holder = mapperWithDeserializer()
                .readValue("{\"id\":{\"id\":\"%s\"}}".formatted(HEX), Holder.class);

        assertThat(holder.id()).isEqualTo(new ObjectId(HEX));
    }

    @Test
    void anObjectWithNoIdInsideItReadsAsNullRatherThanThrowing() {
        // A stored row that cannot be deserialized is an event nobody ever handles.
        assertThat(mapperWithDeserializer().readValue("{\"id\":{\"other\":1}}", Holder.class).id()).isNull();
    }

    @Test
    void anExplicitNullReadsAsNull() {
        assertThat(mapperWithDeserializer().readValue("{\"id\":null}", Holder.class).id()).isNull();
    }

    @Test
    void aRedirectIsBuiltFromTheSchemeAndPortTheBrowserUsed() {
        assertThat(consoleRedirects().getRedirectionUrl("/sign-in")).isEqualTo(SIGN_IN);
    }

    @Test
    void aPathWithoutALeadingSlashGetsOneAndAnEmptyPathGetsNothing() {
        RedirectionUrlBuilder builder = consoleRedirects();

        assertThat(builder.getRedirectionUrl("sign-in")).isEqualTo(SIGN_IN);
        assertThat(builder.getRedirectionUrl("")).isEqualTo(ORIGIN);
        assertThat(builder.getRedirectionUrl("/")).isEqualTo(ORIGIN);
    }
}
