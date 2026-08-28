'use client'

/** Last-resort boundary (root layout itself failed). Theme-less on purpose: nothing below it is trusted. */
export default function GlobalError({error, reset}: { error: Error & { digest?: string }; reset: () => void }) {
    return (
        <html lang="en">
        <body style={{fontFamily: 'system-ui, sans-serif', padding: '4rem 1rem', textAlign: 'center'}}>
        <h1 style={{fontSize: '1.25rem', marginBottom: '0.5rem'}}>Something went wrong</h1>
        <p style={{opacity: 0.7, marginBottom: '1.5rem'}}>{error.digest ? `Reference: ${error.digest}` : 'Please try again.'}</p>
        <button onClick={reset} style={{padding: '0.5rem 1rem', border: '1px solid currentColor', borderRadius: '0.375rem', background: 'transparent', cursor: 'pointer'}}>
            Try again
        </button>
        </body>
        </html>
    );
}
