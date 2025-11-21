let sdk: any = null;

const openTelemetryEndpoint = process.env.OTEL_EXPORTER_OTLP_ENDPOINT;

if (!openTelemetryEndpoint) {
    console.log("⚠️ OpenTelemetry disabled (OTEL_EXPORTER_OTLP_ENDPOINT not set)");
} else {
    console.log(`✅ OpenTelemetry enabled → ${openTelemetryEndpoint}`);
    const {
        NodeSDK
    } = require('@opentelemetry/sdk-node');
    const {
        getNodeAutoInstrumentations
    } = require('@opentelemetry/auto-instrumentations-node');
    const {
        OTLPTraceExporter
    } = require('@opentelemetry/exporter-trace-otlp-http');
    const {
        OTLPMetricExporter
    } = require('@opentelemetry/exporter-metrics-otlp-http');
    const {
        PeriodicExportingMetricReader
    } = require('@opentelemetry/sdk-metrics');

    const traceExporter = new OTLPTraceExporter({});

    const metricExporter = new OTLPMetricExporter({});

    sdk = new NodeSDK({
        traceExporter,
        metricReader: new PeriodicExportingMetricReader({
            exporter: metricExporter,
            exportIntervalMillis: 60000, // Export metrics every 60 seconds
        }),
        instrumentations: [
            getNodeAutoInstrumentations({
                // Customize auto-instrumentation
                '@opentelemetry/instrumentation-fs': {
                    enabled: false, // Disable file system instrumentation to reduce noise
                },
                '@opentelemetry/instrumentation-http': {
                    enabled: true,
                },
                '@opentelemetry/instrumentation-express': {
                    enabled: true,
                },
            }),
        ],
    });

// Start the SDK
    sdk.start();
    console.log('✅ OpenTelemetry instrumentation started');

// Gracefully shut down the SDK on process exit
    process.on('SIGTERM', () => {
        sdk
            .shutdown()
            .then(() => console.log('OpenTelemetry SDK shut down successfully'))
            .catch((error: any) => console.error('Error shutting down OpenTelemetry SDK', error))
            .finally(() => process.exit(0));
    });

}
export default sdk;
