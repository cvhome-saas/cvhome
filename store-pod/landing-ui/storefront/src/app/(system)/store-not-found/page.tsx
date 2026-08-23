import type {Metadata} from 'next';

export const metadata: Metadata = {title: 'Store not found', robots: {index: false}};

/** Rendered (via rewrite, status 404) when a request carries no Store-Id — i.e. no store owns this domain. */
export default function StoreNotFoundPage() {
    return (
        <main className="mx-auto flex min-h-dvh max-w-narrow flex-col items-center justify-center gap-4 px-gutter text-center">
            <p className="text-sm font-medium uppercase tracking-wide text-muted-foreground">404</p>
            <h1 className="text-3xl font-semibold">Store not found</h1>
            <p className="text-muted-foreground">No store is configured for this address.</p>
        </main>
    );
}
