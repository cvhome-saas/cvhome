/**
 * Next.js instrumentation hook — boots the OpenTelemetry NodeSDK once per server process.
 * Port of the former Express `app/src/instrumentation.ts`; exporters read OTEL_EXPORTER_OTLP_* env vars.
 */
export async function register() {
    if (process.env.NEXT_RUNTIME !== 'nodejs') return;
    if (process.env.OTEL_SDK_DISABLED === 'true') return;
    const {startTelemetry} = await import('./src/shell/telemetry');
    startTelemetry();
}
