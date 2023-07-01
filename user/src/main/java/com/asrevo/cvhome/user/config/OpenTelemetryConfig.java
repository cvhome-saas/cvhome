package com.asrevo.cvhome.user.config;

/*
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
*/
import io.opentelemetry.instrumentation.spring.autoconfigure.EnableOpenTelemetry;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableOpenTelemetry
public class OpenTelemetryConfig {
/*
        OpenTelemetryConfig.initializeLogging();
    // Route JUL logs to slf4j
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();


    public static void initializeLogging() {
        OpenTelemetrySdk sdk =
                OpenTelemetrySdk.builder()
                        .setTracerProvider(SdkTracerProvider.builder().setSampler(Sampler.alwaysOn()).build())
                        .setLoggerProvider(
                                SdkLoggerProvider.builder()
                                        .setResource(
                                                Resource.getDefault().toBuilder()
                                                        .put(ResourceAttributes.SERVICE_NAME, "log4j-example")
                                                        .build())
                                        .addLogRecordProcessor(
                                                BatchLogRecordProcessor.builder(
                                                                OtlpGrpcLogRecordExporter.builder()
                                                                        .setEndpoint("http://localhost:4317")
                                                                        .build())
                                                        .build())
                                        .build())
                        .build();
        GlobalOpenTelemetry.set(sdk);

        // Add hook to close SDK, which flushes logs
        Runtime.getRuntime().addShutdownHook(new Thread(sdk::close));
    }
*/
}
