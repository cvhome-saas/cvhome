package org.springframework.cloud.client;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The copy of Spring Cloud's {@code DefaultServiceInstance} this module ships in the framework's own package to
 * override the upstream jar.
 *
 * <p>
 * Because it shadows a class the load balancer instantiates, it has to behave exactly like the original: a URI
 * assignment has to decompose into host, port and — for https — the secure flag, and equality has to compare fields
 * rather than identity, or a routing cache keyed on instances silently stops matching.
 * </p>
 */
class DefaultServiceInstanceTest {

    private static final String INSTANCE_ID = "i-123";

    private static final String SERVICE_ID = "catalog";

    private static final String HOST = "10.0.1.7";

    private static final int PORT = 9091;

    private static final String ZONE = "zone";

    private static final String ZONE_VALUE = "eu-west-1a";

    private static final String OTHER_INSTANCE_ID = "i-999";

    private static final String OTHER_SERVICE_ID = "inventory";

    private static final String OTHER_HOST = "10.0.9.9";

    private static Map<String, String> metadata() {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put(ZONE, ZONE_VALUE);
        return metadata;
    }

    private static DefaultServiceInstance instance() {
        return new DefaultServiceInstance(INSTANCE_ID, SERVICE_ID, HOST, PORT, false, metadata());
    }

    @Test
    void anInstanceReportsWhatItWasBuiltWith() {
        DefaultServiceInstance instance = instance();

        assertThat(instance.getInstanceId()).isEqualTo(INSTANCE_ID);
        assertThat(instance.getServiceId()).isEqualTo(SERVICE_ID);
        assertThat(instance.getHost()).isEqualTo(HOST);
        assertThat(instance.getPort()).isEqualTo(PORT);
        assertThat(instance.isSecure()).isFalse();
        assertThat(instance.getMetadata()).containsEntry(ZONE, ZONE_VALUE);
    }

    @Test
    void anInstanceBuiltWithoutMetadataStillHasAMap() {
        DefaultServiceInstance instance = new DefaultServiceInstance(INSTANCE_ID, SERVICE_ID, HOST, PORT, false);

        assertThat(instance.getMetadata()).isEmpty();
    }

    @Test
    void aPlainInstanceIsAddressedOverHttp() {
        assertThat(instance().getUri()).hasToString("http://10.0.1.7:9091");
        assertThat(instance().getScheme()).isEqualTo("http");
    }

    @Test
    void aSecureInstanceIsAddressedOverHttps() {
        DefaultServiceInstance instance = new DefaultServiceInstance(INSTANCE_ID, SERVICE_ID, HOST, 443, true);

        assertThat(instance.getScheme()).isEqualTo("https");
    }

    @Test
    void assigningAUriDecomposesItIntoHostAndPort() {
        DefaultServiceInstance instance = instance();

        instance.setUri(URI.create("http://10.0.2.9:8080"));

        assertThat(instance.getHost()).isEqualTo("10.0.2.9");
        assertThat(instance.getPort()).isEqualTo(8080);
        assertThat(instance.isSecure()).isFalse();
    }

    @Test
    void assigningAnHttpsUriAlsoMarksTheInstanceSecure() {
        DefaultServiceInstance instance = instance();

        instance.setUri(URI.create("https://10.0.2.9:8443"));

        assertThat(instance.isSecure()).isTrue();
    }

    @Test
    void theSettersReplaceEveryField() {
        DefaultServiceInstance instance = instance();

        instance.setInstanceId(OTHER_INSTANCE_ID);
        instance.setServiceId(OTHER_SERVICE_ID);
        instance.setHost(OTHER_HOST);
        instance.setPort(1234);
        instance.setSecure(true);

        assertThat(instance.getInstanceId()).isEqualTo(OTHER_INSTANCE_ID);
        assertThat(instance.getServiceId()).isEqualTo(OTHER_SERVICE_ID);
        assertThat(instance.getHost()).isEqualTo(OTHER_HOST);
        assertThat(instance.getPort()).isEqualTo(1234);
        assertThat(instance.isSecure()).isTrue();
    }

    @Test
    void twoInstancesDescribingTheSameTaskAreEqual() {
        assertThat(instance()).isEqualTo(instance()).hasSameHashCodeAs(instance());
    }

    @Test
    void instancesDifferingInAnyFieldAreNotEqual() {
        DefaultServiceInstance other = new DefaultServiceInstance(INSTANCE_ID, SERVICE_ID, HOST, 1234, false,
                metadata());

        assertThat(instance()).isNotEqualTo(other).isNotEqualTo(null).isNotEqualTo("not an instance");
    }

    @Test
    void anInstanceIsEqualToItself() {
        DefaultServiceInstance instance = instance();

        assertThat(instance.equals(instance)).isTrue();
    }

    @Test
    void theStringFormNamesTheServiceAndItsAddress() {
        assertThat(instance().toString()).contains(SERVICE_ID, HOST, String.valueOf(PORT));
    }
}
