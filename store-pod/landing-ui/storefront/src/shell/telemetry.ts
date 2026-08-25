import {NodeSDK} from '@opentelemetry/sdk-node';
import {OTLPTraceExporter} from '@opentelemetry/exporter-trace-otlp-grpc';
import {OTLPMetricExporter} from '@opentelemetry/exporter-metrics-otlp-grpc';
import {getNodeAutoInstrumentations} from '@opentelemetry/auto-instrumentations-node';
import {PeriodicExportingMetricReader} from '@opentelemetry/sdk-metrics';
import {Context, propagation, TextMapGetter, TextMapPropagator, TextMapSetter} from '@opentelemetry/api';
import {CompositePropagator, W3CBaggagePropagator, W3CTraceContextPropagator} from '@opentelemetry/core';
import {
    DetectedResource, emptyResource, envDetector, hostDetector, osDetector, ResourceDetectionConfig, ResourceDetector,
    serviceInstanceIdDetector,
} from '@opentelemetry/resources';

const EXCLUDED_RESOURCE_KEYS = new Set([
    'process.command', 'process.command_args', 'process.command_line',
    'process.executable.name', 'process.executable.path', 'process.owner',
    'process.parent_pid', 'process.pid',
    'process.runtime.description', 'process.runtime.name', 'process.runtime.version',
    'telemetry.distro.name', 'telemetry.distro.version',
    'telemetry.sdk.language', 'telemetry.sdk.name', 'telemetry.sdk.version',
    'os.version', 'os.description', 'os.type',
]);

function withKeyFilter(detector: ResourceDetector): ResourceDetector {
    return {
        detect(config?: ResourceDetectionConfig): DetectedResource {
            const {attributes, ...rest} = detector.detect(config);
            if (!attributes) return {attributes, ...rest};
            return {
                ...rest,
                attributes: Object.fromEntries(Object.entries(attributes).filter(([k]) => !EXCLUDED_RESOURCE_KEYS.has(k))),
            };
        },
    };
}

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

let started = false;

export function startTelemetry() {
    if (started) return;
    started = true;
    const sdk = new NodeSDK({
        traceExporter: new OTLPTraceExporter({}),
        serviceName: 'landing-ui',
        resource: emptyResource(),
        resourceDetectors: [envDetector, hostDetector, osDetector, serviceInstanceIdDetector].map(withKeyFilter),
        textMapPropagator: new CompositePropagator({
            propagators: [new W3CTraceContextPropagator(), new BrowserSourcePropagator()],
        }),
        metricReader: new PeriodicExportingMetricReader({
            exporter: new OTLPMetricExporter({}),
            exportIntervalMillis: 60000,
        }),
        instrumentations: [
            getNodeAutoInstrumentations({
                '@opentelemetry/instrumentation-fs': {enabled: false},
                '@opentelemetry/instrumentation-http': {enabled: true},
            }),
        ],
    });
    sdk.start();
    console.log('✅ OpenTelemetry instrumentation started');
    process.on('SIGTERM', () => {
        sdk.shutdown()
            .then(() => console.log('OpenTelemetry SDK shut down successfully'))
            .catch((error) => console.error('Error shutting down OpenTelemetry SDK', error))
            .finally(() => process.exit(0));
    });
}
