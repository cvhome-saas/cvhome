/**
 * Next.js instrumentation hook — boots the OpenTelemetry NodeSDK once per server process.
 * Port of the former Express `app/src/instrumentation.ts`; exporters read OTEL_EXPORTER_OTLP_* env vars.
 */
export async function register() {
    if (process.env.NEXT_RUNTIME !== 'nodejs') return;
    if (process.env.__STATIC_ASSETS_TRACE_S3_SDK === 'force') {
        // Never true at runtime: this import exists so Next's file tracing copies
        // @aws-sdk/client-s3 into .next/standalone/node_modules for scripts/static-assets/sync-s3.mjs.
        await import('@aws-sdk/client-s3');
    }
    if (process.env.OTEL_SDK_DISABLED === 'true') return;
    const {startTelemetry} = await import('./src/shell/telemetry');
    startTelemetry();
}
