import {NodeSDK} from '@opentelemetry/sdk-node';
import {OTLPTraceExporter} from '@opentelemetry/exporter-trace-otlp-grpc';
import {OTLPMetricExporter} from '@opentelemetry/exporter-metrics-otlp-grpc';
import {getNodeAutoInstrumentations} from '@opentelemetry/auto-instrumentations-node';
import {PeriodicExportingMetricReader} from '@opentelemetry/sdk-metrics';
import {Context, propagation, TextMapGetter, TextMapPropagator, TextMapSetter} from '@opentelemetry/api';
import {CompositePropagator, W3CBaggagePropagator, W3CTraceContextPropagator} from '@opentelemetry/core';

class BrowserSourcePropagator implements TextMapPropagator {
    private readonly _baggage = new W3CBaggagePropagator();

    inject(context: Context, carrier: unknown, setter: TextMapSetter): void {
        const existing = propagation.getBaggage(context);
        const withSource = (existing ?? propagation.createBaggage()).setEntry('source', {value: 'browser'});
        this._baggage.inject(propagation.setBaggage(context, withSource), carrier, setter);
    }

    extract(context: Context, carrier: unknown, getter: TextMapGetter): Context {
        return this._baggage.extract(context, carrier, getter);
    }

    fields(): string[] {
        return this._baggage.fields();
    }
}


// Configure the trace exporter
const traceExporter = new OTLPTraceExporter({});

// Configure the metric exporter
const metricExporter = new OTLPMetricExporter({});

// Create the OpenTelemetry SDK
const sdk = new NodeSDK({
    traceExporter,
    serviceName: "landing-ui",
    textMapPropagator: new CompositePropagator({
        propagators: [new W3CTraceContextPropagator(), new BrowserSourcePropagator()],
    }),
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
        .catch((error) => console.error('Error shutting down OpenTelemetry SDK', error))
        .finally(() => process.exit(0));
});

export default sdk;